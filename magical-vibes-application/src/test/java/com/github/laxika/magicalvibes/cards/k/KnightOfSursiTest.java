package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.w.WhipSpineDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfSursi.class, BlindPhantasm.class, WhipSpineDrake.class})
class KnightOfSursiTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Knight of Sursi with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        KnightOfSursi card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        KnightOfSursi card = suspendCard();

        advanceThroughSuspendCountdown();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Knight of Sursi");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Knight of Sursi exiled")
    void decliningSuspendCastLeavesCardExiled() {
        KnightOfSursi card = suspendCard();

        advanceThroughSuspendCountdown();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Knight of Sursi"));
    }

    @Test
    @DisplayName("Flying prevents a creature without flying from blocking Knight of Sursi")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new KnightOfSursi());
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flanking gives a nonflanking blocker -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        addCreatureReady(player1, new KnightOfSursi());
        Permanent blocker = addCreatureReady(player2, new WhipSpineDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    private KnightOfSursi suspendCard() {
        KnightOfSursi card = new KnightOfSursi();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void advanceThroughSuspendCountdown() {
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
    }
}
