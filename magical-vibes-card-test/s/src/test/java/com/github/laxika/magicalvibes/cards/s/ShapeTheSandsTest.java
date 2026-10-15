package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShapeTheSands.class, DragonScarredBear.class, SpidersilkNet.class, Flatten.class})
class ShapeTheSandsTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureAndGrantsReach() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setHand(player1, List.of(new ShapeTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
    }

    @Test
    void boostAndReachWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setHand(player1, List.of(new ShapeTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SpidersilkNet());
        harness.setHand(player1, List.of(new ShapeTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        harness.setHand(player1, List.of(new ShapeTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.REACH)).isFalse();
    }

    @Test
    void multipleCastsStackTheirToughnessBoosts() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setHand(player1, List.of(new ShapeTheSands(), new ShapeTheSands()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isFalse();
    }

    @Test
    void doesNotResolveWhenTargetDiesInResponse() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setHand(player1, List.of(new ShapeTheSands()));
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Dragon-Scarred Bear");
        harness.assertInGraveyard(player1, "Shape the Sands");
        assertThat(gqs.getEffectiveToughness(gd, otherBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBear, Keyword.REACH)).isFalse();
    }
}
