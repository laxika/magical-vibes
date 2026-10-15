package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitFlame.class, DragonEgg.class, GrizzlyBears.class, HillGiant.class})
class SpitFlameTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        prepareMain(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpitFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A Dragon entering lets you pay {R} to return Spit Flame to hand")
    void dragonEntersPayReturnsToHand() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{R}");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(spitFlame.getId()));
    }

    @Test
    @DisplayName("Declining the Dragon trigger keeps Spit Flame in the graveyard")
    void declineKeepsSpitFlameInGraveyard() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
    }

    @Test
    @DisplayName("A non-Dragon creature entering does not trigger")
    void nonDragonDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SpitFlame()));
        prepareMain(player1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A Dragon an opponent controls entering does not trigger")
    void opponentDragonDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SpitFlame()));
        prepareMain(player2);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromHand(player2, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Cannot return Spit Flame without {R}")
    void cannotReturnWithoutRedMana() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);

        harness.castFromHand(player1, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(spitFlame.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(spitFlame.getId()));
    }

    @Test
    @DisplayName("Paying for one copy does not return other copies")
    void eachCopyRequiresItsOwnPayment() {
        SpitFlame first = new SpitFlame();
        SpitFlame second = new SpitFlame();
        harness.setGraveyard(player1, List.of(first, second));
        prepareMain(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An old Dragon trigger cannot return Spit Flame after it is cast again")
    void oldTriggerCannotReturnNewGraveyardObject() {
        SpitFlame spitFlame = new SpitFlame();
        harness.setGraveyard(player1, List.of(spitFlame));
        prepareMain(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.enterBattlefieldAndReturn(player1, new DragonEgg());
        harness.enterBattlefieldAndReturn(player1, new DragonEgg());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Spit Flame");

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Spit Flame");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Spit Flame");
        harness.assertNotInHand(player1, "Spit Flame");
    }
}
