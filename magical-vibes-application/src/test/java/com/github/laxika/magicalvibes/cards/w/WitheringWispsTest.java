package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitheringWispsTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WitheringWisps());
        addSnowSwamp(player1);
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1 dies
        harness.addToBattlefield(player2, new GrizzlyBears());   // 2/2 survives
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot be activated at all with no snow Swamps")
    void cannotActivateWithoutSnowSwamps() {
        harness.addToBattlefield(player1, new WitheringWisps());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow Swamps you control");
    }

    @Test
    @DisplayName("One snow Swamp allows exactly one activation each turn")
    void oneSnowSwampAllowsOneActivation() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WitheringWisps());
        addSnowSwamp(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow Swamps you control");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two snow Swamps allow two activations, killing a 2/2")
    void twoSnowSwampsAllowTwoActivations() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WitheringWisps());
        addSnowSwamp(player1);
        addSnowSwamp(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow Swamps you control");
    }

    @Test
    @DisplayName("Opponent's snow Swamps do not raise the activation limit")
    void opponentSnowSwampsDoNotCount() {
        harness.addToBattlefield(player1, new WitheringWisps());
        addSnowSwamp(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow Swamps you control");
    }

    @Test
    @DisplayName("Non-snow Swamps do not raise the activation limit")
    void plainSwampsDoNotCount() {
        harness.addToBattlefield(player1, new WitheringWisps());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow Swamps you control");
    }

    @Test
    @DisplayName("Sacrifices itself at end step when no creatures are on the battlefield")
    void sacrificesAtEndStepWhenNoCreatures() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new WitheringWisps()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Withering Wisps");
        harness.assertInGraveyard(player1, "Withering Wisps");
    }

    @Test
    @DisplayName("Does not sacrifice itself while a creature is on the battlefield")
    void doesNotSacrificeWhenCreaturePresent() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new WitheringWisps()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Withering Wisps"));
        harness.assertOnBattlefield(player1, "Withering Wisps");
    }

    private void addSnowSwamp(Player player) {
        Permanent snowSwamp = new Permanent(new Swamp());
        TestCards.mutableCard(snowSwamp).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowSwamp);
    }
}
