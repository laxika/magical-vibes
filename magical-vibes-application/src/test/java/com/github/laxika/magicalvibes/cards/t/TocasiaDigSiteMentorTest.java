package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TocasiaDigSiteMentorTest extends BaseCardTest {

    @Test
    void grantsVigilanceAndTapToSurveil() {
        Permanent tocasia = addCreatureReady(player1, new TocasiaDigSiteMentor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

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
