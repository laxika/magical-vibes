package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nevermaker.class, IndomitableAncients.class, Mutavault.class, CloakAndDagger.class})
class NevermakerTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: Nevermaker enters normally and stays on the battlefield")
    void hardcastStaysOnBattlefield() {
        harness.setHand(player1, List.of(new Nevermaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nevermaker");
        harness.assertNotInGraveyard(player1, "Nevermaker");
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB tucks target nonland permanent on top of its owner's library")
    void evokeTucksTargetOnEntrySacrifice() {
        Permanent target = addCreatureReady(player2, new IndomitableAncients());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new Nevermaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        // Nevermaker sacrificed as it entered.
        harness.assertNotOnBattlefield(player1, "Nevermaker");
        // Target tucked on top of its owner's library.
        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Indomitable Ancients");
    }

    @Test
    @DisplayName("LTB fires on any leave and tucks the chosen nonland permanent on top of its owner's library")
    void leavesBattlefieldTucksTarget() {
        Permanent target = addCreatureReady(player2, new IndomitableAncients());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB trigger

        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Indomitable Ancients");
    }

    @Test
    @DisplayName("LTB has no valid target when only a land is available (nonland restriction)")
    void leavesBattlefieldSkipsWhenOnlyLandAvailable() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mutavault());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger has no valid targets -> skipped

        // Land untouched, nothing tucked.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("LTB can target a noncreature nonland permanent")
    void leavesBattlefieldTucksNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloakAndDagger());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB trigger

        harness.assertNotOnBattlefield(player2, "Cloak and Dagger");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Cloak and Dagger");
    }

    @Test
    @DisplayName("Returning Nevermaker to hand also triggers its ability")
    void returningToHandTucksTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, nevermaker));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Nevermaker");
        harness.assertNotInGraveyard(player1, "Nevermaker");
        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(target.getCard());
    }

    @Test
    @DisplayName("Nevermaker can tuck a nonland permanent controlled by its own controller")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CloakAndDagger());
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cloak and Dagger");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(target.getCard());
    }

    @Test
    @DisplayName("A target that leaves before resolution is not put on top of the library")
    void targetLeavingBeforeResolutionIsNotTucked() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        resolveAllTriggers();

        harness.assertInHand(player2, "Indomitable Ancients");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evoke still sacrifices Nevermaker when no nonland target remains")
    void evokeWithoutValidTargetStillSacrifices() {
        harness.addToBattlefield(player2, new Mutavault());
        harness.setHand(player1, List.of(new Nevermaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nevermaker");
        harness.assertNotOnBattlefield(player1, "Nevermaker");
        harness.assertOnBattlefield(player2, "Mutavault");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
