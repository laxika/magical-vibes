package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoneRevenant.class, BorderlandRanger.class, Forest.class, Island.class, Mountain.class,
        PillarOfFlame.class})
class LoneRevenantTest extends BaseCardTest {

    private void attackWithRevenant() {
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, new LoneRevenant());
        revenant.setSummoningSick(false);
        revenant.setAttacking(true);
    }

    private List<Card> stackTopFour() {
        Card top1 = new BorderlandRanger();
        Card top2 = new Forest();
        Card top3 = new Island();
        Card top4 = new Mountain();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, top4);
        deck.add(0, top3);
        deck.add(0, top2);
        deck.add(0, top1);
        return List.of(top1, top2, top3, top4);
    }

    @Test
    @DisplayName("Combat damage with no other creatures looks at top four; chosen card to hand, rest on bottom")
    void triggersWhenAlone() {
        List<Card> top = stackTopFour();
        attackWithRevenant();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(2).getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(2));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("No trigger while another creature is controlled")
    void noTriggerWithAnotherCreature() {
        List<Card> top = stackTopFour();
        attackWithRevenant();
        harness.addToBattlefield(player1, new BorderlandRanger());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top.getFirst());
    }

    @Test
    void chosenBottomOrderPreservesUnlookedCards() {
        List<Card> top = stackTopFour();
        Card unlooked = new Forest();
        harness.setLibrary(player1, List.of(top.get(0), top.get(1), top.get(2), top.get(3), unlooked));
        attackWithRevenant();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top.get(2).getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unlooked, top.get(3), top.get(0), top.get(1));
        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(2))
                .doesNotContain(top.get(0), top.get(1), top.get(3), unlooked);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anotherCreatureEnteringBeforeResolutionStopsTheEffect() {
        List<Card> top = stackTopFour();
        attackWithRevenant();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new BorderlandRanger());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 4)).containsExactlyElementsOf(top);
    }

    @Test
    void removingOtherCreatureAfterDamageDoesNotCreateATrigger() {
        List<Card> top = stackTopFour();
        attackWithRevenant();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        resolveCombat();
        assertThat(gd.stack).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(other);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 4)).containsExactlyElementsOf(top);
    }

    @Test
    void noncreaturePermanentsAndOpponentsCreaturesDoNotPreventTrigger() {
        stackTopFour();
        attackWithRevenant();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new BorderlandRanger());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    void abilityResolvesAfterSourceLeavesWithNoOtherCreatures() {
        List<Card> top = stackTopFour();
        attackWithRevenant();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0));
    }

    @Test
    void oneCardLibraryPutsItsOnlyCardIntoHand() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        attackWithRevenant();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotCauseALossOrRequireAChoice() {
        harness.setLibrary(player1, List.of());
        attackWithRevenant();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void opponentCannotTargetRevenantWithPillarOfFlame() {
        Permanent revenant = harness.addToBattlefieldAndReturn(player2, new LoneRevenant());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, revenant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
    }
}
