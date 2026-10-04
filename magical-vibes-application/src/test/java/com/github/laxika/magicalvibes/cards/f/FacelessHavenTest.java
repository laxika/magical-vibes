package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FacelessHaven.class})
class FacelessHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Faceless Haven produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent haven = addReadyHaven(player1);

        harness.tapPermanent(player1, indexOf(haven));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation ability requires snow mana")
    void animationRequiresSnowMana() {
        addReadyHaven(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Snow mana animates Faceless Haven as a vigilant 4/3 with all creature types")
    void animatesWithSnowMana() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isTrue();
        assertThat(gqs.getEffectivePower(gd, haven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, haven)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, haven, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.isLand(gd, haven)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isFalse();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.isLand(gd, haven)).isTrue();
    }

    @Test
    @DisplayName("Animation grants creature types to Faceless Haven until cleanup")
    void gainsCreatureTypesUntilCleanup() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.GOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.FOREST)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.ELF)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, haven, CardSubtype.GOD)).isFalse();
    }

    @Test
    @DisplayName("Faceless Haven can use its own snow mana while tapped to animate")
    void animatesWhileTappedUsingManaFromSnowLands() {
        Permanent haven = addReadyHaven(player1);
        addReadyHaven(player1);
        addReadyHaven(player1);
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isTrue();
        assertThat(haven.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).get(1))).isFalse();
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).get(2))).isFalse();
    }

    @Test
    @DisplayName("Snow mana of different colors pays the animation cost")
    void animatesWithColoredSnowMana() {
        Permanent haven = addReadyHaven(player1);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.WHITE, 1);
        pool.addSnowMana(ManaColor.BLUE, 1);
        pool.addSnowMana(ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(pool.getSnowManaTotal()).isZero();
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isTrue();
        assertThat(haven.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Faceless Haven attacks without tapping and can still produce mana")
    void vigilanceAllowsAttackingAndProducingMana() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(haven.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);
        assertThat(haven.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animating a newly controlled land does not remove summoning sickness")
    void newlyControlledHavenCannotTapAfterAnimation() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new FacelessHaven());
        haven.setSummoningSick(true);
        addSnowMana(player1, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private Permanent addReadyHaven(Player player) {
        Permanent haven = harness.addToBattlefieldAndReturn(player, new FacelessHaven());
        haven.setSummoningSick(false);
        return haven;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.addSnowMana(ManaColor.COLORLESS, amount);
    }
}
