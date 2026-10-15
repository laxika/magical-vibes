package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
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

@CardUsed({FancyFootwork.class, OtterPenguin.class, Island.class})
class FancyFootworkTest extends BaseCardTest {

    @Test
    @DisplayName("Both target creatures untap and get +2/+2")
    void twoTargetsUntapAndBoost() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();

        cast(first, second);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent creature = addTappedCreature();

        cast(creature);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        cast(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();

        UUID islandId = island.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(islandId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapped opposing creatures can be targeted and boosted")
    void untappedOpponentCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());

        cast(creature);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("At least one target is required")
    void cannotChooseZeroTargets() {
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose three targets")
    void cannotChooseThreeTargets() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();
        Permanent third = addTappedCreature();
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The two targets must be different creatures")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = addTappedCreature();
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving target still untaps and gets boosted")
    void resolvesWhenOneTargetLeavesBattlefield() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untargeted creatures stay tapped and receive no boost")
    void leavesUntargetedCreaturesUnchanged() {
        Permanent target = addTappedCreature();
        Permanent other = addTappedCreature();

        cast(target);

        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    private Permanent addTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        creature.tap();
        return creature;
    }

    private void cast(Permanent... targets) {
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();
        List<UUID> targetIds = targets.length == 1
                ? List.of(targets[0].getId())
                : List.of(targets[0].getId(), targets[1].getId());
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
