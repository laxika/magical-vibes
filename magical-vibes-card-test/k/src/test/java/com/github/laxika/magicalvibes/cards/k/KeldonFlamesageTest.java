package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DestroyEvil;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.PhyrexianEspionage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonFlamesage.class, PhyrexianEspionage.class, LightningStrike.class, DestroyEvil.class})
class KeldonFlamesageTest extends BaseCardTest {

    @Test
    void looksAtTopCardsEqualToPowerAndOnlyOffersCheapInstantOrSorcery() {
        Card tooExpensive = new PhyrexianEspionage();
        Card nonSpell = new KeldonFlamesage();
        Card notLookedAt = new LightningStrike();
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(tooExpensive, nonSpell, notLookedAt));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(tooExpensive, nonSpell, notLookedAt);
    }

    @Test
    void powerSetsHowManyCardsAreLookedAtAndTheFreeCastLimit() {
        Card nonSpell = new KeldonFlamesage();
        Card cheapInstant = new LightningStrike();
        Card eligibleSorcery = new PhyrexianEspionage();
        Card notLookedAt = new KeldonFlamesage();
        Permanent flamesage = addCreatureReady(player1, new KeldonFlamesage());
        flamesage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(nonSpell, cheapInstant, eligibleSorcery, notLookedAt));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(cheapInstant, eligibleSorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notLookedAt);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleSorcery);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(eligibleSorcery);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getCard() == eligibleSorcery);
    }

    @Test
    void decliningTheFreeCastLeavesTheChosenCardInExile() {
        Card eligibleInstant = new LightningStrike();
        Card rest = new KeldonFlamesage();
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(eligibleInstant, rest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleInstant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rest);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == eligibleInstant);
    }

    @Test
    void mayDeclineToExileEvenWhenAnEligibleSpellIsFound() {
        Card eligible = new LightningStrike();
        Card rest = new KeldonFlamesage();
        Card untouched = new PhyrexianEspionage();
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(eligible, rest, untouched));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(eligible, rest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void usesLastKnownBoostedPowerForBothLookCountAndManaValueLimit() {
        Card eligible = new PhyrexianEspionage();
        Card first = new KeldonFlamesage();
        Card third = new KeldonFlamesage();
        Card untouched = new LightningStrike();
        Permanent flamesage = addCreatureReady(player1, new KeldonFlamesage());
        flamesage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(first, eligible, third, untouched));
        harness.setHand(player1, List.of(new DestroyEvil()));

        declareAttackers(List.of(0));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, flamesage.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Keldon Flamesage");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, third);
    }

    @Test
    void emptyLibraryDoesNotOfferAnExileOrCastChoice() {
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void freeCastingAnEligibleSorceryOffersItsAffordableKickerCost() {
        Card eligible = new PhyrexianEspionage();
        Permanent flamesage = addCreatureReady(player1, new KeldonFlamesage());
        flamesage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(eligible));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == eligible);
    }

    @Test
    void enlistingTapsANonattackingSupporterAndAddsItsPower() {
        Permanent flamesage = addCreatureReady(player1, new KeldonFlamesage());
        Permanent supporter = addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(new KeldonFlamesage(), new KeldonFlamesage()));

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(supporter.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flamesage)).isEqualTo(4);
        assertThat(supporter.isAttacking()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }
}
