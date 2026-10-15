package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.cards.s.Syncopate;
import com.github.laxika.magicalvibes.cards.w.WildsongHowler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlpackPiper.class, WildsongHowler.class, SporebackWolf.class, SporeCrawler.class, Syncopate.class, Forest.class})
class HowlpackPiperTest extends BaseCardTest {

    @Test
    @DisplayName("A Wolf put onto the battlefield untaps Howlpack Piper")
    void wolfUntapsPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new SporebackWolf()));
        addAbilityMana();

        activateAndResolveCardChoice();

        assertThat(piper.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sporeback Wolf");
    }

    @Test
    @DisplayName("A non-Wolf creature put onto the battlefield does not untap Howlpack Piper")
    void nonWolfDoesNotUntapPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new SporeCrawler()));
        addAbilityMana();

        activateAndResolveCardChoice();

        assertThat(piper.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Spore Crawler");
    }

    @Test
    @DisplayName("Wildsong Howler looks at six cards when it enters at night")
    void wildsongHowlerLooksAtTopSixOnEntry() {
        gd.dayNight = DayNight.NIGHT;
        Card wolf = new SporebackWolf();
        harness.setLibrary(player1, List.of(
                new Forest(), wolf, new Forest(), new Forest(), new Forest(), new Forest()));

        Permanent howler = harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
        assertThat(howler.getCard()).isInstanceOf(WildsongHowler.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(wolf.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforming into Wildsong Howler triggers its library ability")
    void wildsongHowlerLooksAtTopSixWhenItTransforms() {
        gd.dayNight = DayNight.DAY;
        Card wolf = new SporebackWolf();
        harness.setLibrary(player1, List.of(
                new Forest(), wolf, new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent howler = harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(wolf.getId());
    }


    @Test
    void werewolfUntapsPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new HowlpackPiper()));
        addAbilityMana();

        activateAndResolveCardChoice();

        assertThat(piper.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningCreatureLeavesPiperTappedAndCardInHand() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new SporebackWolf()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(piper.isTapped()).isTrue();
        harness.assertInHand(player1, "Sporeback Wolf");
        harness.assertNotOnBattlefield(player1, "Sporeback Wolf");
    }

    @Test
    void spellCannotBeCountered() {
        HowlpackPiper piper = new HowlpackPiper();
        harness.setHand(player1, List.of(piper));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, piper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Howlpack Piper");
        harness.assertNotInGraveyard(player1, "Howlpack Piper");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void howlerSelectsOneCreatureAndBottomsOnlyLookedAtCards() {
        gd.dayNight = DayNight.NIGHT;
        Card wolf = new SporebackWolf();
        Card crawler = new SporeCrawler();
        List<Card> rest = List.of(crawler, new Forest(), new Forest(), new Forest(), new Forest());
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(wolf, rest.get(0), rest.get(1), rest.get(2),
                rest.get(3), rest.get(4), untouched));

        harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(wolf.getId()));

        harness.assertInHand(player1, "Sporeback Wolf");
        harness.assertNotInHand(player1, "Spore Crawler");
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(untouched);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void howlerCanDeclineOnlyCreatureInShortLibrary() {
        gd.dayNight = DayNight.NIGHT;
        Card wolf = new SporebackWolf();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(wolf, forest));

        harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Sporeback Wolf");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(wolf, forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void howlerWithNoCreaturesFinishesWithoutChoice() {
        gd.dayNight = DayNight.NIGHT;
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nightboundTransformsBackWithoutLibraryTrigger() {
        gd.dayNight = DayNight.NIGHT;
        harness.setLibrary(player1, List.of());
        Permanent piper = harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(piper.isTransformed()).isFalse();
        assertThat(piper.getCard()).isInstanceOf(HowlpackPiper.class);
        assertThat(gd.stack).isEmpty();
    }


    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent piper = addReadyPiper();
        addAbilityMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(piper.isTapped()).isFalse();
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        Permanent piper = addReadyPiper();
        addAbilityMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(piper.isTapped()).isFalse();
    }

    @Test
    void nonCreatureHandCannotUntapPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new Forest()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(piper.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyPiper() {
        Permanent piper = new Permanent(new HowlpackPiper());
        piper.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piper);
        return piper;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateAndResolveCardChoice() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
