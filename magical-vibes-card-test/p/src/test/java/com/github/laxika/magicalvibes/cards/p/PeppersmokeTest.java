package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChangelingTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.n.NectarFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Peppersmoke.class, AirElemental.class, GrizzlyBears.class, NectarFaerie.class,
        HillcomberGiant.class, ChangelingTitan.class})
class PeppersmokeTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new Peppersmoke()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Gives target creature -1/-1")
    void appliesBoost() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4
        castOn(elemental);

        assertThat(elemental.getPowerModifier()).isEqualTo(-1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-1);
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("-1/-1 kills a 1/1 creature")
    void killsSmallCreature() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player2, new NectarFaerie()); // 1/1
        castOn(faerie);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(faerie);
    }

    @Test
    @DisplayName("Draws a card if you control a Faerie")
    void drawsWithFaerie() {
        harness.addToBattlefield(player1, new NectarFaerie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castOn(target);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a Faerie")
    void noDrawWithoutFaerie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castOn(target);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("-1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);
        assertThat(elemental.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(0);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void drawsBeforeItsOnlyFaerieDies() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        castOn(faerie);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(faerie);
    }

    @Test
    void opponentsFaerieDoesNotAllowDraw() {
        harness.addToBattlefield(player2, new NectarFaerie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        castOn(target);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void changelingCountsAsFaerie() {
        harness.addToBattlefield(player1, new ChangelingTitan());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setLibrary(player1, List.of(new HillcomberGiant()));

        castOn(target);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsIfFaerieArrivesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new Peppersmoke()));
        harness.setLibrary(player1, List.of(new HillcomberGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new NectarFaerie());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawIfOnlyFaerieDiesInResponse() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new Peppersmoke()));
        harness.setHand(player2, List.of(new Peppersmoke()));
        harness.setLibrary(player1, List.of(new HillcomberGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, faerie.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void illegalTargetPreventsDrawEvenWithAnotherFaerie() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());
        harness.addToBattlefield(player1, new NectarFaerie());
        harness.setHand(player1, List.of(new Peppersmoke()));
        harness.setHand(player2, List.of(new Peppersmoke()));
        harness.setLibrary(player1, List.of(new HillcomberGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).hasSize(1);
    }
}
