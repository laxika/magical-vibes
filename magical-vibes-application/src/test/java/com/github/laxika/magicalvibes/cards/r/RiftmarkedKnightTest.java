package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SaltfieldRecluse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiftmarkedKnight.class, SaltfieldRecluse.class})
class RiftmarkedKnightTest extends BaseCardTest {

    @Test
    void suspendExilesRiftmarkedKnightWithThreeTimeCounters() {
        RiftmarkedKnight card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void removingAnEarlierTimeCounterDoesNotCreateTheKnightToken() {
        suspendCard();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isZero();
    }

    @Test
    void opponentsUpkeepDoesNotRemoveTimeCounters() {
        RiftmarkedKnight card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(countPermanents(player2, "Knight")).isZero();
    }

    @Test
    void knightHasProtectionFromBlack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new RiftmarkedKnight());

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.WHITE)).isFalse();
    }

    @Test
    void flankingWeakensNonFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new RiftmarkedKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SaltfieldRecluse());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void flankingDoesNotWeakenABlockerWithFlanking() {
        Permanent knight = addCreatureReady(player1, new RiftmarkedKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RiftmarkedKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    void acceptingSuspendCastCreatesBothKnightAndToken() {
        RiftmarkedKnight card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(countPermanents(player1, "Riftmarked Knight")).isEqualTo(1);
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(countPermanents(player2, "Knight")).isZero();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Riftmarked Knight"), Keyword.HASTE)).isTrue();
    }

    @Test
    void addingATimeCounterAfterLastCounterTriggerDoesNotPreventTokenCreation() {
        RiftmarkedKnight card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Knight")).isZero();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
    }

    @Test
    void lastTimeCounterCreatesHastyFlankingKnightWithProtectionFromWhite() {
        suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Knight");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLANKING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, token, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, token, CardColor.BLACK)).isFalse();
    }

    private RiftmarkedKnight suspendCard() {
        RiftmarkedKnight card = new RiftmarkedKnight();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
