package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtrataTheSilencer.class, BartizanBats.class, UnexplainedDisappearance.class,
        HuntedWitness.class, DeadWeight.class})
class EtrataTheSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles a chosen defending creature with a hit counter and shuffles Etrata")
    void exilesChosenDefendingCreatureAndShufflesSource() {
        Permanent etrata = addAttackingEtrata();
        Permanent ownCreature = addCreatureReady(player1, new BartizanBats());
        Permanent opposingCreature = addCreatureReady(player2, new BartizanBats());

        resolveEtrataTrigger(opposingCreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingCreature.getCard());
        assertThat(gd.exiledCardHitCounters).containsEntry(opposingCreature.getCard().getId(), 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(etrata);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(etrata.getCard());
    }

    @Test
    @DisplayName("The combat trigger only offers creatures controlled by the damaged player")
    void targetsOnlyDamagedPlayersCreatures() {
        addAttackingEtrata();
        Permanent ownCreature = addCreatureReady(player1, new BartizanBats());
        Permanent opposingCreature = addCreatureReady(player2, new BartizanBats());

        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingCreature.getId()).doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("The damaged player loses after their third owned exiled hit-counter card")
    void thirdHitCounterCardMakesDamagedPlayerLose() {
        Card first = new BartizanBats();
        Card second = new BartizanBats();
        gd.addToExile(player2.getId(), first);
        gd.addToExile(player2.getId(), second);
        gd.exiledCardHitCounters.put(first.getId(), 1);
        gd.exiledCardHitCounters.put(second.getId(), 1);

        Permanent etrata = addAttackingEtrata();
        Permanent opposingCreature = addCreatureReady(player2, new BartizanBats());

        resolveEtrataTrigger(opposingCreature);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.exiledCardHitCounters).containsEntry(opposingCreature.getCard().getId(), 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(etrata.getCard());
    }

    @Test
    void cannotBeBlockedEvenByFlyingCreature() {
        addAttackingEtrata();
        addCreatureReady(player2, new BartizanBats());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void remainsOnBattlefieldWhenDamagedPlayerHasNoCreatures() {
        Permanent etrata = addAttackingEtrata();

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etrata);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(etrata.getCard());
    }

    @Test
    void bouncedEtrataRemainsInHandWhenItsTriggerResolves() {
        Permanent etrata = addAttackingEtrata();
        Permanent target = addCreatureReady(player2, new BartizanBats());
        harness.setLibrary(player1, List.of());
        resolveCombat();
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handlePermanentChosen(player1, target.getId());

        harness.castAndResolveInstant(player1, 0, etrata.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(etrata.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(etrata.getCard());
    }

    @Test
    void illegalTargetPreventsExileAndSourceShuffle() {
        Permanent etrata = addAttackingEtrata();
        Permanent target = addCreatureReady(player2, new BartizanBats());
        harness.setLibrary(player1, List.of());
        resolveCombat();
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handlePermanentChosen(player1, target.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etrata);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(etrata.getCard());
    }

    @Test
    void countsCardsRatherThanNumberOfHitCounters() {
        Card first = new BartizanBats();
        gd.addToExile(player2.getId(), first);
        gd.exiledCardHitCounters.put(first.getId(), 3);
        addAttackingEtrata();
        Permanent target = addCreatureReady(player2, new BartizanBats());

        resolveEtrataTrigger(target);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void exilingBorrowedCreatureCountsForItsOwnerNotItsController() {
        for (int i = 0; i < 2; i++) {
            Card hitCard = new BartizanBats();
            gd.addToExile(player2.getId(), hitCard);
            gd.exiledCardHitCounters.put(hitCard.getId(), 1);
        }
        addAttackingEtrata();
        Card borrowed = new BartizanBats();
        borrowed.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player2, borrowed);

        resolveEtrataTrigger(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(borrowed);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(borrowed);
        assertThat(gd.exiledCardHitCounters).containsEntry(borrowed.getId(), 1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void checksExistingHitCardsEvenWhenExilingToken(int existingHitCards) {
        Permanent witness = addCreatureReady(player2, new HuntedWitness());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, witness.getId());
        resolveAllTriggers();
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        for (int i = 0; i < existingHitCards; i++) {
            Card hitCard = new BartizanBats();
            gd.addToExile(player2.getId(), hitCard);
            gd.exiledCardHitCounters.put(hitCard.getId(), 1);
        }
        Permanent etrata = addAttackingEtrata();

        resolveEtrataTrigger(token);

        if (existingHitCards >= 3) {
            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        } else {
            assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        }
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(token.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).contains(etrata.getCard());
    }

    private Permanent addAttackingEtrata() {
        Permanent etrata = addCreatureReady(player1, new EtrataTheSilencer());
        etrata.setAttacking(true);
        return etrata;
    }

    private void resolveEtrataTrigger(Permanent target) {
        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
