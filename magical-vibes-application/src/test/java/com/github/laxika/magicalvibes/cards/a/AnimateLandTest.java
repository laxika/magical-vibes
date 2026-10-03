package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AnimateLand.class, KorHaven.class, Mossdog.class})
class AnimateLandTest extends BaseCardTest {

    @Test
    @DisplayName("Animates target land into a 3/3 creature that is still a land")
    void animatesTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        castAnimateLand(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Can animate a land an opponent controls")
    void animatesOpponentLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        castAnimateLand(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        castAnimateLand(land);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Mossdog());
        harness.setHand(player1, List.of(new AnimateLand()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animating a tapped land does not untap it")
    void tappedLandRemainsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        harness.tapPermanent(player1, 0);

        castAnimateLand(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animated land retains its mana ability")
    void retainsManaAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        land.setSummoningSick(false);
        castAnimateLand(land);

        harness.tapPermanent(player1, 0);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Only the targeted land becomes a creature")
    void doesNotAnimateOtherLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new KorHaven());

        castAnimateLand(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, otherLand)).isFalse();
    }

    @Test
    @DisplayName("Animation sets base power and toughness before applying counters")
    void countersApplyOnTopOfAnimation() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAnimateLand(land);

        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
    }

    private void castAnimateLand(Permanent target) {
        harness.setHand(player1, List.of(new AnimateLand()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
