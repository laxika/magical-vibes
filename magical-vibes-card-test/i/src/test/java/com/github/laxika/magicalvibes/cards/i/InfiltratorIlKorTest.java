package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiltratorIlKor.class, BladeOfTheSixthPride.class})
class InfiltratorIlKorTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Infiltrator il-Kor with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        InfiltratorIlKor card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        InfiltratorIlKor card = suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);

        Permanent permanent = findPermanent(player1, card.getName());
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove Infiltrator il-Kor's suspend counters")
    void opponentUpkeepDoesNotRemoveSuspendCounter() {
        InfiltratorIlKor card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    @DisplayName("Declining the free suspend cast leaves Infiltrator il-Kor exiled")
    void decliningFreeCastLeavesCardExiled() {
        InfiltratorIlKor card = suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Infiltrator il-Kor cannot be blocked by a creature without shadow")
    void cannotBeBlockedByCreatureWithoutShadow() {
        Permanent blocker = setUpCombat(new BladeOfTheSixthPride());

        assertThatThrownBy(() -> declareBlock(blocker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Infiltrator il-Kor cannot block a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        Permanent blocker = setUpCombat(new InfiltratorIlKor(), new BladeOfTheSixthPride());

        assertThatThrownBy(() -> declareBlock(blocker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Infiltrator il-Kor can be blocked by a creature with shadow")
    void canBeBlockedByCreatureWithShadow() {
        Permanent blocker = setUpCombat(new InfiltratorIlKor());

        declareBlock(blocker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The first upkeep removes only one time counter without offering a cast")
    void firstUpkeepRemovesOnlyOneCounter() {
        InfiltratorIlKor card = suspendCard();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Infiltrator il-Kor cannot be suspended during upkeep")
    void cannotSuspendDuringUpkeep() {
        InfiltratorIlKor card = new InfiltratorIlKor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Casting Infiltrator il-Kor normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        InfiltratorIlKor card = new InfiltratorIlKor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, card.getName());
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
        assertThat(permanent.isSummoningSick()).isTrue();
    }

    private InfiltratorIlKor suspendCard() {
        InfiltratorIlKor card = new InfiltratorIlKor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private Permanent setUpCombat(Card blockerCard) {
        return setUpCombat(new InfiltratorIlKor(), blockerCard);
    }

    private Permanent setUpCombat(Card blockerCard, Card attackerCard) {
        Permanent blocker = addCreatureReady(player2, blockerCard);

        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        return blocker;
    }

    private void declareBlock(Permanent blocker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).size() - 1;
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
