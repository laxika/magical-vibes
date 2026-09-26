package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoungNecromancer.class, GrizzlyBears.class, Island.class})
class YoungNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles two cards and returns a targeted creature")
    void exilesTwoCardsAndReanimatesCreature() {
        Card target = new GrizzlyBears();
        Card first = new Island();
        Card second = new Island();
        harness.setGraveyard(player1, List.of(target, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Declining the ETB ability does not exile or return cards")
    void decliningDoesNothing() {
        Card target = new GrizzlyBears();
        Card first = new Island();
        Card second = new Island();
        harness.setGraveyard(player1, List.of(target, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Fewer than two cards cannot pay the optional exile")
    void fewerThanTwoCardsDoNothing() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    private void castYoungNecromancer() {
        harness.setHand(player1, List.of(new YoungNecromancer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
