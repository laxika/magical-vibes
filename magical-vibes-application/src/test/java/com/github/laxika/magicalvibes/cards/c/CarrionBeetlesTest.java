package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrionBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three target cards from a single graveyard")
    void exilesThreeCardsFromSingleGraveyard() {
        Permanent beetles = addReadyBeetles();
        Card first = new GrizzlyBears();
        Card second = new LightningBolt();
        Card third = new GrizzlyBears();
        Card untouched = new LightningBolt();
        harness.setGraveyard(player2, List.of(first, second, third, untouched));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(beetles), 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(beetles.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Up to three allows exiling fewer cards")
    void exilesFewerThanThreeCards() {
        Permanent beetles = addReadyBeetles();
        Card target = new GrizzlyBears();
        Card untouched = new LightningBolt();
        harness.setGraveyard(player1, List.of(target, untouched));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(beetles), 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot select cards from multiple graveyards")
    void rejectsTargetsFromMultipleGraveyards() {
        Permanent beetles = addReadyBeetles();
        Card ownCard = new GrizzlyBears();
        Card opposingCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(beetles), 0, List.of(ownCard.getId(), opposingCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(beetles.isTapped()).isFalse();
    }

    private Permanent addReadyBeetles() {
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CarrionBeetles());
        beetles.setSummoningSick(false);
        return beetles;
    }

    private int index(Permanent beetles) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(beetles);
    }
}
