package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrahvSpiresOfOrder.class, GrizzlyBears.class})
class PrahvSpiresOfOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage prevention ability requires its mana cost and tap")
    void preventionAbilityRequiresManaAndTap() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        addPreventionMana();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the prevention ability prompts for a source")
    void promptsForSource() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(source.getId());
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new PrahvSpiresOfOrder());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addPreventionMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(source.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(source.getId());
    }

    private void addPreventionMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
