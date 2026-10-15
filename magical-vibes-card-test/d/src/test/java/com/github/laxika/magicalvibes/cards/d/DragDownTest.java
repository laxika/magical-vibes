package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragDown.class, AvatarOfMight.class, FountainOfYouth.class, Forest.class,
        GrizzlyBears.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        TerramorphicExpanse.class})
class DragDownTest extends BaseCardTest {

    @Test
    @DisplayName("One basic land type gives -1/-1 (2/2 -> 1/1)")
    void oneBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domain scales: three basic land types give -3/-3 (8/8 -> 5/5)")
    void threeBasicLandTypesScale() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getEffectivePower()).isEqualTo(5);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once (two Forests -> -1/-1)")
    void duplicateTypesCountOnce() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getEffectivePower()).isEqualTo(7);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Drag Down wears off at cleanup step")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Forest());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untyped lands and the opponent's basic lands do not contribute to domain")
    void zeroDomainWithUntypedLandAndOpposingBasics() {
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Drag Down");
    }

    @Test
    @DisplayName("All five basic land types give -5/-5 to an opposing creature")
    void fiveBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getEffectivePower()).isEqualTo(3);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reducing toughness to zero puts the target in its owner's graveyard")
    void zeroToughnessKillsTarget() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drag Down");
    }

    @Test
    @DisplayName("Domain is counted at resolution and stays fixed afterward")
    void countsLandTypesAtResolutionOnly() {
        harness.addToBattlefield(player1, new Forest());
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, avatar.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(avatar.getEffectivePower()).isEqualTo(6);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(6);

        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(6);
    }
}
