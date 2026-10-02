package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.d.DouserOfLights;
import com.github.laxika.magicalvibes.cards.n.NightveilSprite;
import com.github.laxika.magicalvibes.cards.w.WishcoinCrab;
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

@CardUsed({ArtfulTakedown.class, DimirGuildgate.class, DouserOfLights.class,
        NightveilSprite.class, WishcoinCrab.class})
class ArtfulTakedownTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps the target creature")
    void tapModeTapsTargetCreature() {
        Permanent creature = addCreatureReady(player2, new WishcoinCrab());

        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Debuff mode gives the target -2/-4 until end of turn")
    void debuffModeReducesPowerAndToughness() {
        Permanent creature = addCreatureReady(player2, new DouserOfLights());

        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Both modes resolve on the same target creature")
    void bothModesResolveOnSameTarget() {
        Permanent creature = addCreatureReady(player2, new DouserOfLights());

        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modes require creature targets")
    void modesRejectNonCreatureTarget() {
        assertThatThrownBy(() -> {
            harness.setHand(player1, List.of(new ArtfulTakedown()));
            addMana();
            harness.castModalInstantWithModes(player1, 0, 1, 2,
                    new int[]{0}, List.of(player2.getId()));
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("Both modes affect only their respective targets")
    void bothModesResolveOnDifferentTargets() {
        Permanent tapTarget = addCreatureReady(player2, new WishcoinCrab());
        Permanent debuffTarget = addCreatureReady(player1, new DouserOfLights());

        cast(new int[]{0, 1}, List.of(tapTarget.getId(), debuffTarget.getId()));

        assertThat(tapTarget.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, tapTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tapTarget)).isEqualTo(5);
        assertThat(debuffTarget.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, debuffTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, debuffTarget)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap mode can target an already tapped creature")
    void tapModeAcceptsTappedCreature() {
        Permanent creature = addCreatureReady(player1, new WishcoinCrab());
        creature.setTapped(true);

        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Artful Takedown");
    }

    @Test
    @DisplayName("Debuff mode puts a creature with nonpositive toughness into the graveyard")
    void debuffModeKillsCreature() {
        Permanent creature = addCreatureReady(player2, new NightveilSprite());

        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Nightveil Sprite");
    }

    @Test
    @DisplayName("An illegal tap target does not stop the debuff on the other target")
    void debuffResolvesWhenTapTargetLeavesBattlefield() {
        Permanent tapTarget = addCreatureReady(player2, new WishcoinCrab());
        Permanent debuffTarget = addCreatureReady(player2, new DouserOfLights());
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(tapTarget.getId(), debuffTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(tapTarget);

        harness.passBothPriorities();

        assertThat(debuffTarget.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, debuffTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, debuffTarget)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Artful Takedown");
    }

    @Test
    @DisplayName("An illegal debuff target does not stop the tap on the other target")
    void tapResolvesWhenDebuffTargetLeavesBattlefield() {
        Permanent tapTarget = addCreatureReady(player2, new WishcoinCrab());
        Permanent debuffTarget = addCreatureReady(player2, new DouserOfLights());
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(tapTarget.getId(), debuffTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(debuffTarget);

        harness.passBothPriorities();

        assertThat(tapTarget.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, tapTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tapTarget)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Artful Takedown");
    }

    @Test
    @DisplayName("Both modes do nothing when their shared target leaves the battlefield")
    void bothModesDoNothingWhenSharedTargetLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new DouserOfLights());
        Permanent other = addCreatureReady(player2, new WishcoinCrab());
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artful Takedown");
    }

    @Test
    @DisplayName("Debuff mode rejects a player target")
    void debuffModeRejectsPlayerTarget() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature permanent")
    void tapModeRejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DimirGuildgate());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Debuff mode rejects a noncreature permanent")
    void debuffModeRejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DimirGuildgate());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ArtfulTakedown()));
        addMana();
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
