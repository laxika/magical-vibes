package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveDroid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoyaDeathFromAbove.class, FugitiveDroid.class})
class KoyaDeathFromAboveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and returns it when the controller declines to pay")
    void declinesPaymentAndReturnsCreature() {
        Permanent droid = harness.addToBattlefieldAndReturn(player2, new FugitiveDroid());
        castKoyaTargeting(droid);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(droid);
        assertThat(gd.findExiledCard(droid.getCard().getId())).isNotNull();

        advanceToNextEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(droid.getCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(droid.getCard().getId()));
    }

    @Test
    @DisplayName("Paying {3}{B} keeps the exiled creature in exile")
    void paysAndKeepsCreatureExiled() {
        Permanent droid = harness.addToBattlefieldAndReturn(player2, new FugitiveDroid());
        castKoyaTargeting(droid);

        advanceToNextEndStep();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(droid.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(droid);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Choosing no target still creates the delayed payment ability")
    void noTargetStillOffersPaymentAtNextEndStep() {
        harness.setHand(player1, List.of(new KoyaDeathFromAbove()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToNextEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertOnBattlefield(player1, "Koya, Death from Above");
    }

    @Test
    @DisplayName("Returns at the end step of the same turn")
    void returnsAtSameTurnEndStep() {
        Permanent droid = harness.addToBattlefieldAndReturn(player2, new FugitiveDroid());
        castKoyaTargeting(droid);
        int turnNumber = gd.turnNumber;

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.turnNumber).isEqualTo(turnNumber);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(droid.getCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Fugitive Droid");
    }

    @Test
    @DisplayName("Leaving the battlefield does not stop Koya's delayed return")
    void returnsAfterKoyaLeavesBattlefield() {
        Permanent droid = harness.addToBattlefieldAndReturn(player2, new FugitiveDroid());
        castKoyaTargeting(droid);
        Permanent koya = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KoyaDeathFromAbove)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, koya);

        advanceToNextEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Koya, Death from Above");
        harness.assertOnBattlefield(player2, "Fugitive Droid");
    }

    @Test
    @DisplayName("Can exile another creature controlled by Koya's controller")
    void canExileOwnCreature() {
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new FugitiveDroid());
        castKoyaTargeting(droid);
        assertThat(gd.findExiledCard(droid.getCard().getId())).isNotNull();

        advanceToNextEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(droid.getCard().getId())).isNull();
        harness.assertOnBattlefield(player1, "Fugitive Droid");
        harness.assertNotOnBattlefield(player2, "Fugitive Droid");
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        FugitiveDroid card = new FugitiveDroid();
        card.setOwnerId(player2.getId());
        Permanent droid = harness.addToBattlefieldAndReturn(player1, card);
        castKoyaTargeting(droid);

        advanceToNextEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertOnBattlefield(player2, "Fugitive Droid");
        harness.assertNotOnBattlefield(player1, "Fugitive Droid");
    }

    private void castKoyaTargeting(Permanent target) {
        harness.setHand(player1, List.of(new KoyaDeathFromAbove()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextEndStep() {
        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
