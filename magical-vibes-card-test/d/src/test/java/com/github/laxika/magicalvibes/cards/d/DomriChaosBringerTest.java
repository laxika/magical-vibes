package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TerritorialBoar;
import com.github.laxika.magicalvibes.cards.g.GruulBeastmaster;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DomriChaosBringer.class, TerritorialBoar.class, AxebaneBeast.class,
        Deface.class, StompingGround.class, GruulBeastmaster.class})
class DomriChaosBringerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds chosen mana that gives a creature spell riot")
    void plusOneGrantsRiotToCreatureSpellPaidWithMana() {
        Permanent domri = addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getRiotGrantingManaTotal()).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new TerritorialBoar()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Territorial Boar");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 puts up to two creature cards from the top four into hand")
    void minusThreePutsUpToTwoCreaturesIntoHand() {
        addReadyDomri(player1);
        Card bears = new TerritorialBoar();
        Card bolt = new Deface();
        Card angel = new AxebaneBeast();
        Card plains = new StompingGround();
        harness.setLibrary(player1, List.of(bears, bolt, angel, plains));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), angel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), angel.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears, angel);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bolt, plains);
    }

    @Test
    @DisplayName("-8 creates an emblem that makes a Beast at each end step")
    void minusEightCreatesEndStepBeastEmblem() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void plusOneCanGrantHasteInsteadOfACounter() {
        addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new TerritorialBoar()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent boar = findPermanent(player1, "Territorial Boar");
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, boar, Keyword.HASTE)).isTrue();
    }

    @Test
    void domriManaAddsAnIndependentRiotInstance() {
        addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new GruulBeastmaster()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        Permanent beastmaster = findPermanent(player1, "Gruul Beastmaster");
        assertThat(beastmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, beastmaster, Keyword.HASTE)).isTrue();
    }

    @Test
    void minusThreeCanDeclineAllCreaturesAndLeavesUnseenCardsOnTop() {
        addReadyDomri(player1);
        Card boar = new TerritorialBoar();
        Card beast = new AxebaneBeast();
        Card deface = new Deface();
        Card land = new StompingGround();
        Card unseen = new GruulBeastmaster();
        harness.setLibrary(player1, List.of(boar, beast, deface, land, unseen));
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(boar, beast, deface, land, unseen);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusThreeCanChooseOnlyOneCreatureFromAShortLibrary() {
        addReadyDomri(player1);
        Card boar = new TerritorialBoar();
        Card beast = new AxebaneBeast();
        harness.setLibrary(player1, List.of(boar, beast));
        harness.setHand(player1, List.of());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(boar.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(boar);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(beast);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusThreeWithNoCreaturesBottomsOnlyTheTopFour() {
        addReadyDomri(player1);
        Card first = new Deface();
        Card second = new StompingGround();
        Card third = new Deface();
        Card fourth = new StompingGround();
        Card unseen = new TerritorialBoar();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
        harness.setHand(player1, List.of());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, third, fourth, unseen);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emblemTriggersOnOpponentsEndStepAfterDomriLeaves() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(domri);
        assertThat(gd.emblems).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
        assertThat(beast.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyDomri(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DomriChaosBringer());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
