package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragDownTest extends BaseCardTest {

    @Test
    @DisplayName("One basic land type gives -1/-1 (2/2 -> 1/1)")
    void oneBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domain scales: three basic land types give -3/-3 (8/8 -> 5/5)")
    void threeBasicLandTypesScale() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID avatarId = harness.getPermanentId(player1, "Avatar of Might");
        harness.castInstant(player1, 0, avatarId);
        harness.passBothPriorities();

        Permanent avatar = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Avatar of Might")).findFirst().orElseThrow();
        assertThat(avatar.getEffectivePower()).isEqualTo(5);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once (two Forests -> -1/-1)")
    void duplicateTypesCountOnce() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID avatarId = harness.getPermanentId(player1, "Avatar of Might");
        harness.castInstant(player1, 0, avatarId);
        harness.passBothPriorities();

        Permanent avatar = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Avatar of Might")).findFirst().orElseThrow();
        assertThat(avatar.getEffectivePower()).isEqualTo(7);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Drag Down wears off at cleanup step")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
