package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseFall.class, Forest.class, GrizzlyBears.class})
class RiseFallTest extends BaseCardTest {

    @Test
    void riseReturnsTheTargetGraveyardCreatureAndBattlefieldCreatureToTheirOwnersHands() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(battlefieldCreature.getId()));
    }

    @Test
    void riseCannotTargetANonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(graveyardCreature.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallDiscardsTheRevealedNonlandCards() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void fallKeepsARevealedLandInHand() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(land, creature));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }
}
