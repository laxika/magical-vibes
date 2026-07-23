package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalduvianConjurerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting the snow land")
    void activatingPutsOnStack() {
        addReadyConjurer(player1);
        Permanent snowLand = addSnowLand(player1);

        harness.activateAbility(player1, 0, null, snowLand.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(snowLand.getId());
    }

    @Test
    @DisplayName("Resolving animates target snow land into a 2/2 creature that is still a land")
    void animatesSnowLandIntoCreature() {
        addReadyConjurer(player1);
        Permanent snowLand = addSnowLand(player1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(snowLand.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(snowLand.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addReadyConjurer(player1);
        Permanent snowLand = addSnowLand(player1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        snowLand.resetModifiers();

        assertThat(snowLand.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addReadyConjurer(player1);
        Permanent plains = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(plains);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadyConjurer(player1);
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyConjurer(Player player) {
        Permanent perm = new Permanent(new BalduvianConjurer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
        return snowLand;
    }
}
