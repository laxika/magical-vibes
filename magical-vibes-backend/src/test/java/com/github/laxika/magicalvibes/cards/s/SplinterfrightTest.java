package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplinterfrightTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Splinterfright has static P/T effect counting creature cards in controller's graveyard")
    void hasCorrectStaticEffect() {
        Splinterfright card = new Splinterfright();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCardsInControllerGraveyardEffect.class);
        var effect = (PowerToughnessEqualToCardsInControllerGraveyardEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isEqualTo(new CardTypePredicate(CardType.CREATURE));
    }

    @Test
    @DisplayName("Splinterfright has upkeep-triggered mill 2 effect")
    void hasUpkeepMillEffect() {
        Splinterfright card = new Splinterfright();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MillControllerEffect.class);
        var effect = (MillControllerEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Splinterfright puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Splinterfright()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Splinterfright");
    }

    // ===== Dynamic power/toughness =====

    @Test
    @DisplayName("Splinterfright is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Splinterfright P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Splinterfright does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addSplinterfrightReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Splinterfright only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addSplinterfrightReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    // ===== Upkeep mill trigger =====

    @Test
    @DisplayName("Splinterfright mills 2 cards at controller's upkeep")
    void millsAtUpkeep() {
        Permanent perm = addSplinterfrightReady(player1);
        // Ensure Splinterfright has at least 1 toughness so it survives SBAs after mill resolves
        harness.setGraveyard(player1, createCreatureCards(1));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Mill trigger does NOT fire during opponent's upkeep")
    void millDoesNotFireDuringOpponentUpkeep() {
        Permanent perm = addSplinterfrightReady(player1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Milled creature cards increase Splinterfright's P/T")
    void milledCreaturesIncreasePT() {
        Permanent perm = addSplinterfrightReady(player1);

        // Put creature cards on top of library so they get milled
        gd.playerDecks.get(player1.getId()).clear();
        List<Card> deck = new ArrayList<>();
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears()); // extra card so library isn't empty
        gd.playerDecks.get(player1.getId()).addAll(deck);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve mill trigger

        // 2 creature cards milled into graveyard
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addSplinterfrightReady(Player player) {
        Splinterfright card = new Splinterfright();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
