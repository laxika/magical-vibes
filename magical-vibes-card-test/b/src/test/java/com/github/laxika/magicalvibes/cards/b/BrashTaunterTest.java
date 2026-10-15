package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrashTaunter.class, GrizzlyBears.class, Shock.class, com.github.laxika.magicalvibes.cards.f.Forest.class})
class BrashTaunterTest extends BaseCardTest {

    @Test
    @DisplayName("Damage dealt to Brash Taunter is dealt to an opponent")
    void reflectsDamageToOpponent() {
        addCreatureReady(player2, new BrashTaunter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID taunterId = harness.getPermanentId(player2, "Brash Taunter");
        harness.castInstant(player1, 0, taunterId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Brash Taunter");
    }

    @Test
    @DisplayName("The activated ability makes Brash Taunter fight another creature")
    void fightsAnotherCreature() {
        Permanent taunter = addCreatureReady(player1, new BrashTaunter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taunter);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(taunter.getMarkedDamage()).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-creature")
    void cannotTargetLand() {
        addCreatureReady(player1, new BrashTaunter());
        Permanent land = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("The damage trigger requires choosing an opponent")
    void damageTriggerRequiresOpponentTarget() {
        Permanent taunter = addCreatureReady(player1, new BrashTaunter());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, taunter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Brash Taunter cannot fight itself")
    void cannotTargetItself() {
        Permanent taunter = addCreatureReady(player1, new BrashTaunter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, taunter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("Brash Taunter can fight another creature its controller owns")
    void canFightOwnCreature() {
        Permanent taunter = addCreatureReady(player1, new BrashTaunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(taunter.isTapped()).isTrue();
        assertThat(taunter.getMarkedDamage()).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Brash Taunter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}