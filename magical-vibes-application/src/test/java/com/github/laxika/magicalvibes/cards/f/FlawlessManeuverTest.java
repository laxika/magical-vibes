package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlawlessManeuver.class, EdgarMarkov.class, GrizzlyBears.class})
class FlawlessManeuverTest extends BaseCardTest {

    @Test
    void normalCastMakesOnlyOwnCreaturesIndestructibleUntilEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlawlessManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.INDESTRUCTIBLE)).isFalse();

        ownCreature.resetModifiers();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlawlessManeuver()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new FlawlessManeuver()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
