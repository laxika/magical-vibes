package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LiegeOfThePit;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.p.PlagueSliver;
import com.github.laxika.magicalvibes.cards.t.TendrilsOfCorruption;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskriderPeregrine.class, TendrilsOfCorruption.class, PlagueSliver.class,
        LiegeOfThePit.class, PithingNeedle.class})
class DuskriderPeregrineTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Duskrider Peregrine with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        DuskriderPeregrine card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend counters are removed only during the owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        DuskriderPeregrine card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        DuskriderPeregrine card = suspendCard();

        removeOneTimeCounter();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        removeOneTimeCounter();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Duskrider Peregrine");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card in exile")
    void decliningCastLeavesCardInExile() {
        DuskriderPeregrine card = suspendCard();
        removeOneTimeCounter();
        removeOneTimeCounter();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(card);
    }

    @Test
    @DisplayName("Protection from black prevents a black spell from targeting Duskrider Peregrine")
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent peregrine = harness.addToBattlefieldAndReturn(player1, new DuskriderPeregrine());
        harness.setHand(player2, List.of(new TendrilsOfCorruption()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, peregrine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking Duskrider Peregrine")
    void protectionFromBlackPreventsBlocking() {
        Permanent peregrine = addCreatureReady(player1, new DuskriderPeregrine());
        peregrine.setAttacking(true);
        addCreatureReady(player2, new LiegeOfThePit());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new PlagueSliver());
        attacker.setAttacking(true);
        Permanent peregrine = addCreatureReady(player1, new DuskriderPeregrine());
        peregrine.setBlocking(true);
        peregrine.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(peregrine.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new DuskriderPeregrine()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent peregrine = findPermanent(player1, "Duskrider Peregrine");
        assertThat(gqs.hasKeyword(gd, peregrine, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Suspend cannot be used during the opponent's main phase")
    void cannotSuspendDuringOpponentsTurn() {
        DuskriderPeregrine card = new DuskriderPeregrine();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Declining suspend does not offer another cast on the next upkeep")
    void declinedCastIsNotOfferedOnLaterUpkeep() {
        DuskriderPeregrine card = suspendCard();
        removeOneTimeCounter();
        removeOneTimeCounter();
        removeOneTimeCounter();
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertNotOnBattlefield(player1, "Duskrider Peregrine");
    }

    @Test
    @CardUsed({DuskriderPeregrine.class, PithingNeedle.class})
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        Permanent needle = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        needle.setChosenName("Duskrider Peregrine");

        DuskriderPeregrine card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    private DuskriderPeregrine suspendCard() {
        DuskriderPeregrine card = new DuskriderPeregrine();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void removeOneTimeCounter() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
