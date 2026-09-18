package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormfangCrab.class, GiantWarthog.class})
class WormfangCrabTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses an eligible permanent you control to exile")
    void opponentChoosesPermanentToExile() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        Permanent crab = castCrab();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId())
                .doesNotContain(crab.getId(), opponentPermanent.getId());

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, crab.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(first.getCard());
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability returns the exiled permanent")
    void leavesTheBattlefieldReturnsExiledPermanent() {
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent crab = castCrab();
        harness.handlePermanentChosen(player2, exiled.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crab));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(exiled.getCard());
        assertThat(gd.exiledCards)
                .noneMatch(entry -> crab.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Its enters-the-battlefield ability does nothing when you control no other permanent")
    void doesNothingWhenYouControlNoOtherPermanent() {
        Permanent crab = castCrab();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crab);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's choice is automatic when only one eligible permanent exists")
    void automaticallyExilesOnlyEligiblePermanent() {
        Permanent onlyEligible = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent crab = castCrab();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(onlyEligible);
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, crab.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(onlyEligible.getCard());
    }

    @Test
    @DisplayName("The returned permanent goes to its owner's battlefield")
    void leavesTheBattlefieldReturnsExiledPermanentToItsOwner() {
        GiantWarthog ownedByPlayer2 = new GiantWarthog();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, ownedByPlayer2);
        harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent crab = castCrab();
        harness.handlePermanentChosen(player2, exiled.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crab));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ownedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .contains(ownedByPlayer2);
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability also returns the card when Crab is exiled")
    void leavesTheBattlefieldReturnsExiledPermanentWhenCrabIsExiled() {
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent crab = castCrab();
        harness.handlePermanentChosen(player2, exiled.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, crab));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(exiled.getCard());
        assertThat(gd.exiledCards)
                .noneMatch(entry -> crab.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("If it leaves before its enters-the-battlefield ability resolves, the card stays exiled")
    void leavingBeforeEnterTriggerResolvesLeavesCardExiled() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.castFromHand(player1, new WormfangCrab(), "{3}{U}");
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Wormfang Crab");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crab));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, crab.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(first.getCard());
    }

    @Test
    @DisplayName("Wormfang Crab cannot be blocked")
    void cannotBeBlocked() {
        Permanent crab = addCreatureReady(player1, new WormfangCrab());
        crab.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantWarthog());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castCrab() {
        harness.castFromHand(player1, new WormfangCrab(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Wormfang Crab");
    }

}
