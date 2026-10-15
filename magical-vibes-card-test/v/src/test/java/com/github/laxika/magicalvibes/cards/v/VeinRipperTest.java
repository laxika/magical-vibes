package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DeadlyCoverUp;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeinRipper.class, GiantGrowth.class, GrizzlyBears.class, Shock.class, DeadlyCoverUp.class})
class VeinRipperTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature dies, target opponent loses 2 life and you gain 2 life")
    void creatureDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new VeinRipper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Death trigger cannot target its controller")
    void deathTriggerCannotTargetController() {
        harness.addToBattlefield(player1, new VeinRipper());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward can be paid by sacrificing a creature")
    void wardCanBePaidBySacrificingCreature() {
        harness.addToBattlefield(player1, new VeinRipper());
        var fodder = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID rippersId = harness.getPermanentId(player1, "Vein Ripper");
        harness.castInstant(player2, 0, rippersId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, fodder.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vein Ripper");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Growth");
    }

    @Test
    void ownDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new VeinRipper());
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Vein Ripper");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void simultaneousDeathsTriggerEachRipperForItselfAndTheOtherCreature() {
        harness.addToBattlefield(player1, new VeinRipper());
        harness.addToBattlefield(player1, new VeinRipper());
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 12);
    }

    @Test
    void wardCountersSpellWhenOpponentHasNoCreatureToSacrifice() {
        var ripper = harness.addToBattlefieldAndReturn(player1, new VeinRipper());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ripper.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(ripper.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningWardLeavesCreatureAliveAndCountersSpell() {
        var ripper = harness.addToBattlefieldAndReturn(player1, new VeinRipper());
        harness.addToBattlefield(player2, new VeinRipper());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ripper.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Vein Ripper");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(ripper.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerSpellDoesNotTriggerWard() {
        var ripper = harness.addToBattlefieldAndReturn(player1, new VeinRipper());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ripper.getId());

        assertThat(ripper.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
