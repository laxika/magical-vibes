package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.h.HarmattanEfreet;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaveElemental.class, FemerefScouts.class, HarmattanEfreet.class, Island.class})
class WaveElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Taps three target creatures without flying and sacrifices itself")
    void tapsThreeCreatures() {
        Permanent elemental = addCreatureReady(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent ownCreature = addCreatureReady(player1, new FemerefScouts());
        Permanent opponentCreatureA = addCreatureReady(player2, new FemerefScouts());
        Permanent opponentCreatureB = addCreatureReady(player2, new FemerefScouts());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(ownCreature.getId(), opponentCreatureA.getId(), opponentCreatureB.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreatureA.isTapped()).isTrue();
        assertThat(opponentCreatureB.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
    }

    @Test
    @DisplayName("Up to three — a single target is legal")
    void tapsSingleCreature() {
        addCreatureReady(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent creature = addCreatureReady(player2, new FemerefScouts());
        Permanent other = addCreatureReady(player2, new FemerefScouts());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        addCreatureReady(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent flyer = addCreatureReady(player2, new HarmattanEfreet());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(flyer.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose zero targets and still pay the activation costs")
    void canChooseZeroTargets() {
        Permanent elemental = addCreatureReady(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new WaveElemental());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        Permanent elemental = addCreatureReady(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent elemental = addCreatureReady(player1, new WaveElemental());
        elemental.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WaveElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("A target that gains flying before resolution is not tapped")
    void targetMustStillNotHaveFlyingAtResolution() {
        addCreatureReady(player1, new WaveElemental());
        addCreatureReady(player2, new HarmattanEfreet());
        Permanent targetThatGainsFlying = addCreatureReady(player2, new FemerefScouts());
        Permanent targetWithoutFlying = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(targetThatGainsFlying.getId(), targetWithoutFlying.getId()));
        harness.activateAbility(player2, 0, 0, null, targetThatGainsFlying.getId());

        harness.passBothPriorities();
        assertThat(targetThatGainsFlying.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passBothPriorities();

        assertThat(targetThatGainsFlying.isTapped()).isFalse();
        assertThat(targetWithoutFlying.isTapped()).isTrue();
    }
}
