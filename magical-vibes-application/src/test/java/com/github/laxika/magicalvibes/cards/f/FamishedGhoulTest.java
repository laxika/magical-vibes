package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AbandonedOutpost;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FamishedGhoul.class, AngelicWall.class, AbandonedOutpost.class})
class FamishedGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose zero cards from a graveyard")
    void canChooseZeroCards() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card available = new AngelicWall();
        harness.setGraveyard(player2, List.of(available));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ghoul);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ghoul.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(available);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself and exiles two cards from one graveyard")
    void sacrificesSelfAndExilesTwoCards() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card card1 = new AngelicWall();
        Card card2 = new AbandonedOutpost();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ghoul);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ghoul.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(card1.getId(), card2.getId());
    }

    @Test
    @DisplayName("Can exile only one card from the controller's graveyard")
    void exilesFewerCardsFromOwnGraveyard() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card chosen = new AngelicWall();
        Card remaining = new AbandonedOutpost();
        harness.setGraveyard(player1, new ArrayList<>(List.of(chosen, remaining)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(remaining.getId(), ghoul.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(chosen.getId());
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card mine = new AngelicWall();
        Card theirs = new AbandonedOutpost();
        harness.setGraveyard(player1, new ArrayList<>(List.of(mine)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(theirs)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card target = new AngelicWall();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target more than two cards")
    void cannotTargetMoreThanTwoCards() {
        Permanent ghoul = addCreatureReady(player1, new FamishedGhoul());
        Card card1 = new AngelicWall();
        Card card2 = new AbandonedOutpost();
        Card card3 = new AngelicWall();
        harness.setGraveyard(player2, List.of(card1, card2, card3));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(card1.getId(), card2.getId(), card3.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 2");
    }

    private int ghoulIndex(Permanent ghoul) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
    }

}
