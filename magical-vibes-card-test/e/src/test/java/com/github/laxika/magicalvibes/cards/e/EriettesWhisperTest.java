package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.TatteredRatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EriettesWhisper.class, TatteredRatter.class})
class EriettesWhisperTest extends BaseCardTest {

    @Test
    void opponentDiscardsTwoAndWickedRoleBoostsTargetCreature() {
        Card firstCard = new TatteredRatter();
        Card secondCard = new TatteredRatter();
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void creatureTargetCanBeOmitted() {
        Card firstCard = new TatteredRatter();
        Card secondCard = new TatteredRatter();
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void replacingWickedRoleTriggersItsLifeLoss() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player2, List.of(
                new TatteredRatter(), new TatteredRatter(), new TatteredRatter(), new TatteredRatter()));
        harness.setHand(player1, List.of(new EriettesWhisper(), new EriettesWhisper()));
        addMana(2);

        castAndResolve(target);
        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        int lifeBeforeReplacement = gd.playerLifeTotals.get(player2.getId());

        castAndResolve(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeReplacement - 1);
    }

    @Test
    void onlyOpponentsCanBeTargetedForDiscard() {
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void createsRoleEvenWhenOpponentHasNoCards() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), target.getId()));

        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void opponentWithOneCardDiscardsItAndRoleIsStillCreated() {
        Card onlyCard = new TatteredRatter();
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void opponentChoosesWhichTwoCardsToDiscard() {
        Card retained = new TatteredRatter();
        Card firstDiscard = new TatteredRatter();
        Card secondDiscard = new TatteredRatter();
        harness.setHand(player2, List.of(retained, firstDiscard, secondDiscard));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void opponentCreatureCannotBeTargetedForRole() {
        Permanent target = addCreatureReady(player2, new TatteredRatter());
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureLeavingBeforeResolutionDoesNotPreventDiscard() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        Card firstCard = new TatteredRatter();
        Card secondCard = new TatteredRatter();
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void roleGoingToGraveyardWhenEnchantedCreatureDiesCostsOpponentOneLife() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), target.getId()));

        target.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        harness.assertInGraveyard(player1, "Tattered Ratter");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    private void castAndResolve(Permanent target) {
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
    }

    private void addMana() {
        addMana(1);
    }

    private void addMana(int casts) {
        harness.addMana(player1, ManaColor.BLACK, casts);
        harness.addMana(player1, ManaColor.COLORLESS, 3 * casts);
    }
}
