package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostbridgeGuard.class, GrizzlyBears.class, Forest.class})
class FrostbridgeGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyGuard(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps the guard and pays its mana cost")
    void activatingPaysCostAndTapsGuard() {
        Permanent guard = addReadyGuard(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(guard.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addReadyGuard(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyGuard(player1);
        Permanent land = addReadyLand(player2);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addReadyGuard(Player player) {
        Permanent permanent = new Permanent(new FrostbridgeGuard());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
