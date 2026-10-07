package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.w.WanderersTwig;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgeOfThoughtweft.class, GoldmeadowStalwart.class, HillcomberGiant.class, WanderersTwig.class})
class SurgeOfThoughtweftTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures you control +1/+1")
    void boostsOwnCreatures() {
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        cast();

        assertThat(firstGiant.getEffectivePower()).isEqualTo(4);
        assertThat(firstGiant.getEffectiveToughness()).isEqualTo(4);
        assertThat(secondGiant.getEffectivePower()).isEqualTo(4);
        assertThat(secondGiant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        cast();

        assertThat(enemy.getEffectivePower()).isEqualTo(3);
        assertThat(enemy.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost noncreature permanents")
    void doesNotBoostNoncreaturePermanents() {
        Permanent twig = harness.addToBattlefieldAndReturn(player1, new WanderersTwig());

        cast();

        assertThat(twig.getEffectivePower()).isEqualTo(0);
        assertThat(twig.getEffectiveToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Draws a card if you control a Kithkin")
    void drawsWithKithkin() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        cast();

        // Hand held only the Surge, which left on resolution; the Kithkin draw refills it.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw a card without a Kithkin")
    void noDrawWithoutKithkin() {
        harness.addToBattlefield(player1, new HillcomberGiant());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw for an opponent's Kithkin")
    void noDrawForOpponentsKithkin() {
        harness.addToBattlefield(player1, new HillcomberGiant());
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        cast();
        assertThat(giant.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The Kithkin spell itself does not satisfy the draw condition")
    void doesNotDrawOnEmptyBattlefield() {
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Checks for a Kithkin when the spell resolves")
    void drawsForKithkinEnteringBeforeResolution() {
        harness.setLibrary(player1, List.of(new HillcomberGiant()));
        harness.castFromHand(player1, new SurgeOfThoughtweft(), "{1}{W}");
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(kithkin.getEffectivePower()).isEqualTo(3);
        assertThat(kithkin.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostLaterCreatures() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        cast();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        assertThat(original.getEffectivePower()).isEqualTo(4);
        assertThat(original.getEffectiveToughness()).isEqualTo(4);
        assertThat(later.getEffectivePower()).isEqualTo(3);
        assertThat(later.getEffectiveToughness()).isEqualTo(3);
    }

    private void cast() {
        harness.castFromHand(player1, new SurgeOfThoughtweft(), "{1}{W}");
        harness.passBothPriorities();
    }
}
