package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BoundaryLandsRanger;
import com.github.laxika.magicalvibes.cards.f.FeedTheCauldron;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsMark.class, Forest.class, BoundaryLandsRanger.class, FeedTheCauldron.class})
class WitchsMarkTest extends BaseCardTest {

    @Test
    void discardingDrawsTwoAndCreatesWickedRole() {
        Card discarded = new BoundaryLandsRanger();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        setDeck(firstDraw, secondDraw);
        harness.setHand(player1, List.of(new WitchsMark(), discarded));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void decliningDiscardStillCreatesWickedRole() {
        Card kept = new BoundaryLandsRanger();
        Card topCard = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        setDeck(topCard);
        harness.setHand(player1, List.of(new WitchsMark(), kept));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void creatureTargetCanBeOmitted() {
        Card kept = new BoundaryLandsRanger();
        harness.setHand(player1, List.of(new WitchsMark(), kept));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BoundaryLandsRanger());
        harness.setHand(player1, List.of(new WitchsMark()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsTwoWithoutChoosingACreature() {
        Card discarded = new BoundaryLandsRanger();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        setDeck(firstDraw, secondDraw);
        harness.setHand(player1, List.of(new WitchsMark(), discarded));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void emptyHandCannotDrawButStillCreatesRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        Card topCard = new Forest();
        setDeck(topCard);
        harness.setHand(player1, List.of(new WitchsMark()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void illegalTargetPreventsDiscardAndDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        Card kept = new Forest();
        Card topCard = new Forest();
        setDeck(topCard);
        harness.setHand(player1, List.of(new WitchsMark(), kept));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId()));

        harness.setHand(player2, List.of(new FeedTheCauldron()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void replacingWickedRoleKeepsOnlyNewestAndDrainsOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new WitchsMark(), new WitchsMark()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);
        Permanent firstRole = findPermanent(player1, "Wicked");

        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(findPermanent(player1, "Wicked").getId()).isNotEqualTo(firstRole.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void losingEnchantedCreaturePutsRoleIntoGraveyardAndDrainsOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new WitchsMark()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void setDeck(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
