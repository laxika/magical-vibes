package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectrumSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TocasiaDigSiteMentor.class, GrizzlyBears.class, SpectrumSentinel.class})
class TocasiaDigSiteMentorTest extends BaseCardTest {

    @Test
    void grantsVigilanceAndTapToSurveil() {
        Permanent tocasia = addCreatureReady(player1, new TocasiaDigSiteMentor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        assertThat(gqs.hasKeyword(gd, tocasia, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tocasia), 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void returnsAnyNumberOfArtifactCardsWithinTotalManaValueLimit() {
        Card tocasia = new TocasiaDigSiteMentor();
        Card fourManaArtifact = artifact("Four-mana artifact", "{4}");
        Card sixManaArtifact = artifact("Six-mana artifact", "{6}");
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(tocasia, fourManaArtifact, sixManaArtifact, creature));
        addTocasiaMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(fourManaArtifact.getId(), sixManaArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(fourManaArtifact.getId(), sixManaArtifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(tocasia.getId()));
    }

    @Test
    void rejectsTargetsOverTotalManaValueLimitBeforePayingCost() {
        Card tocasia = new TocasiaDigSiteMentor();
        Card sixManaArtifact = artifact("Six-mana artifact", "{6}");
        Card fiveManaArtifact = artifact("Five-mana artifact", "{5}");
        harness.setGraveyard(player1, List.of(tocasia, sixManaArtifact, fiveManaArtifact));
        addTocasiaMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(sixManaArtifact.getId(), fiveManaArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tocasia.getId(), sixManaArtifact.getId(), fiveManaArtifact.getId());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(tocasia.getId()));
    }

    @Test
    void mayChooseNoArtifactsButStillExilesThisCardAsACost() {
        Card tocasia = new TocasiaDigSiteMentor();
        harness.setGraveyard(player1, List.of(tocasia));
        addTocasiaMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(tocasia.getId()));
    }

    @Test
    void anotherCreatureCanSurveilAndKeepTheTopCard() {
        addCreatureReady(player1, new TocasiaDigSiteMentor());
        Permanent sentinel = addCreatureReady(player1, new SpectrumSentinel());
        Card topCard = new SpectrumSentinel();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(sentinel.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGrantAbilitiesToOpposingCreaturesAndGrantsEndWhenTocasiaLeaves() {
        Permanent tocasia = addCreatureReady(player1, new TocasiaDigSiteMentor());
        Permanent own = addCreatureReady(player1, new SpectrumSentinel());
        Permanent opposing = addCreatureReady(player2, new SpectrumSentinel());

        assertThat(gqs.hasKeyword(gd, opposing, Keyword.VIGILANCE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player1.getId()).remove(tocasia);
        assertThat(gqs.hasKeyword(gd, own, Keyword.VIGILANCE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        addCreatureReady(player1, new TocasiaDigSiteMentor());
        harness.addToBattlefield(player1, new SpectrumSentinel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonArtifactsAndArtifactsInOpponentsGraveyard() {
        Card tocasia = new TocasiaDigSiteMentor();
        Card opposingArtifact = new SpectrumSentinel();
        harness.setGraveyard(player1, List.of(tocasia));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        addTocasiaMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(tocasia.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opposingArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tocasia);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void graveyardAbilityRequiresOwnMainPhaseAndEmptyStack() {
        Card tocasia = new TocasiaDigSiteMentor();
        harness.setGraveyard(player1, List.of(tocasia));
        addTocasiaMana();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of())).isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of())).isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new TocasiaDigSiteMentor());
        harness.activateAbility(player1, 0, 0, null, null);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tocasia);
        assertThat(gd.exiledCards).isEmpty();
        harness.setLibrary(player1, List.of(new SpectrumSentinel()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetLeavesGraveyard() {
        Card tocasia = new TocasiaDigSiteMentor();
        Card removed = new SpectrumSentinel();
        Card remaining = new SpectrumSentinel();
        harness.setGraveyard(player1, List.of(tocasia, removed, remaining));
        addTocasiaMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(removed.getId(), remaining.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tocasia);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(tocasia.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(removed);
        gd.playerHands.get(player1.getId()).add(removed);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(remaining.getId());
        assertThat(findPermanent(player1, "Spectrum Sentinel").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(removed);
    }

    private Card artifact(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return card;
    }

    private void addTocasiaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
