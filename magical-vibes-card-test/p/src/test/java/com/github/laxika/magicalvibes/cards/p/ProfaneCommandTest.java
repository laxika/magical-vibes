package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfaneCommand.class, GrizzlyBears.class, HillGiant.class, SerraAngel.class})
class ProfaneCommandTest extends BaseCardTest {

    // Mode indices: 0 = target player loses X life,
    //               1 = return creature card MV≤X from GY to battlefield,
    //               2 = target creature gets -X/-X until end of turn,
    //               3 = up to X target creatures gain fear until end of turn.

    @Test
    @DisplayName("Lose-life + -X/-X: burns a player and shrinks a creature")
    void loseLifeAndShrinkCreature() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 2}, 3,
                List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears"); // 2/2 with -3/-3 dies
    }

    @Test
    @DisplayName("Reanimate + fear: returns a MV≤X creature and grants fear to up to X creatures")
    void reanimateAndGrantFear() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID gyBearsId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        UUID angelId = harness.addToBattlefieldAndReturn(player1, new SerraAngel()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();

        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 3}, 2, gyBearsId,
                List.of(angelId, giantId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent angel = gqs.findPermanentById(gd, angelId);
        Permanent giant = gqs.findPermanentById(gd, giantId);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 1,
                List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Reanimate rejects a creature card with mana value greater than X")
    void reanimateRejectsManaValueAboveX() {
        harness.setGraveyard(player1, List.of(new SerraAngel())); // MV 5
        UUID gyAngelId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 2}, 2, gyAngelId,
                        List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear mode with X=0 chooses no creatures and still resolves the other mode")
    void fearModeWithXZeroChoosesNoCreatures() {
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 2); // X=0 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 0,
                List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life loss and reanimation both use the paid X")
    void loseLifeAndReanimateBelowX() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID graveyardId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 1}, 3,
                graveyardId, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reanimation and shrinking affect their separate targets")
    void reanimateAndShrink() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID graveyardId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 2}, 2,
                graveyardId, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The same creature can be targeted by shrinking and fear")
    void sameCreatureCanBeTargetedByBothModes() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{2, 3}, 1,
                List.of(giant.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear may target no creatures even when X is positive")
    void positiveXAllowsNoFearTargets() {
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 3,
                List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Profane Command");
    }

    @Test
    @DisplayName("Fear can target more than one hundred creatures when X allows it")
    void fearHasNoHundredCreatureLimit() {
        List<Permanent> creatures = new java.util.ArrayList<>();
        List<UUID> targets = new java.util.ArrayList<>();
        harness.setLife(player2, 200);
        targets.add(player2.getId());
        for (int i = 0; i < 101; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            creatures.add(creature);
            targets.add(creature.getId());
        }
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 103);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 101, targets);
        harness.passBothPriorities();

        harness.assertLife(player2, 99);
        assertThat(creatures).allSatisfy(creature ->
                assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue());
    }

    @Test
    @DisplayName("Reanimation cannot target an opponent's graveyard")
    void reanimationRejectsOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        UUID graveyardId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 2, new int[]{0, 1}, 2, graveyardId, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reanimation rejects a noncreature card")
    void reanimationRejectsNoncreatureCard() {
        harness.setGraveyard(player1, List.of(new ProfaneCommand()));
        UUID graveyardId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 2, new int[]{0, 1}, 2, graveyardId, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life loss still resolves when the graveyard target disappears")
    void legalPlayerTargetResolvesWhenGraveyardTargetIsGone() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID graveyardId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 1}, 2,
                graveyardId, List.of(player2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Profane Command");
    }

    @Test
    @DisplayName("Fear rejects more than X target creatures")
    void fearRejectsTooManyTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 2, new int[]{0, 3}, 1,
                List.of(player2.getId(), bears.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
