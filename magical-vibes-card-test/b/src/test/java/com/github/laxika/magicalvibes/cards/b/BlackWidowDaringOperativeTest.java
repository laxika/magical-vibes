package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackWidowDaringOperative.class, GrizzlyBears.class, LightningBolt.class})
class BlackWidowDaringOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Black Widow mills three cards when it enters")
    void millsThreeCardsOnEnter() {
        List<Card> milledCards = List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears());
        harness.setLibrary(player1, milledCards);
        harness.setHand(player1, List.of(new BlackWidowDaringOperative()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }

    @Test
    @DisplayName("Combat damage makes each opponent lose life for each creature in the controller's graveyard")
    void combatDamageDrainsEachOpponentByCreatureGraveyardCount() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));

        Permanent blackWidow = addCreatureReady(player1, new BlackWidowDaringOperative());
        blackWidow.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // 3 combat damage plus 2 life loss for the two creature cards in the graveyard.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are counted")
    void countsOnlyControllerCreatureCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent blackWidow = addCreatureReady(player1, new BlackWidowDaringOperative());
        blackWidow.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Entering with fewer than three cards mills only the remaining cards")
    void millsShortLibrary() {
        List<Card> cards = List.of(new BlackWidowDaringOperative(), new LightningBolt());
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new BlackWidowDaringOperative()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature count is evaluated when the combat damage trigger resolves")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());
        addCreatureReady(player1, new BlackWidowDaringOperative()).setAttacking(true);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new BlackWidowDaringOperative(), new LightningBolt()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new BlackWidowDaringOperative());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BlackWidowDaringOperative());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
