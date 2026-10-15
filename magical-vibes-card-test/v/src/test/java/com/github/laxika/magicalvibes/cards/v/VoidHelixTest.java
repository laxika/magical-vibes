package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidHelix.class, GrizzlyBears.class})
class VoidHelixTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a player and gains 5 life")
    void dealsDamageToPlayerAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        prepareVoidHelix();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Deals 5 damage to a creature and gains 5 life")
    void dealsDamageToCreatureAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareVoidHelix();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Self-targeting at 5 life gains life before the player can lose")
    void selfTargetingAtFiveLifeDoesNotLoseTheGame() {
        harness.setLife(player1, 5);
        prepareVoidHelix();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
        assertThat(gd.status)
                .isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life gain belongs to the caster when the opponent casts Void Helix")
    void opponentCasterGainsTheLife() {
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new VoidHelix()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An illegal sole target prevents the life gain as well as the damage")
    void doesNotGainLifeWhenCreatureTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VoidHelix(), new VoidHelix()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castInstant(player1, 0, bears.getId());
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Void Helix");
    }

    private void prepareVoidHelix() {
        harness.setHand(player1, List.of(new VoidHelix()));
        harness.addMana(player1, ManaColor.BLACK, 6);
    }
}
