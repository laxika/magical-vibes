package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianFrostwaker.class, SnowCoveredIsland.class, Plains.class})
class BalduvianFrostwakerTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target snow land into a permanent 2/2 blue Elemental with flying")
    void animatesTargetSnowLand() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, snowLand)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, snowLand)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.FLYING)).isTrue();
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
    }

    @Test
    @DisplayName("Permanent animation survives end-of-turn cleanup")
    void animationSurvivesCleanup() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, snowLand)).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's snow land")
    void canTargetOpponentsSnowLand() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, snowLand)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, snowLand)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.FLYING)).isTrue();
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addReadyFrostwaker(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation taps Frostwaker and cannot be repeated while tapped")
    void activationRequiresUntappedSource() {
        Permanent frostwaker = addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        assertThat(frostwaker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Frostwaker cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BalduvianFrostwaker());
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    @Test
    @DisplayName("Activation requires blue mana")
    void cannotActivateWithoutBlueMana() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    @Test
    @DisplayName("A tapped snow land remains tapped when animated")
    void animationDoesNotUntapTarget() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        snowLand.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(snowLand.isTapped()).isTrue();
    }

    private Permanent addReadyFrostwaker(Player player) {
        return addCreatureReady(player, new BalduvianFrostwaker());
    }

    private Permanent addSnowLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredIsland());
    }
}
