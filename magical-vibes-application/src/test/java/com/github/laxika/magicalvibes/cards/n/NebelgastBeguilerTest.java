package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NebelgastBeguiler.class, GrizzlyBears.class, AngelsFeather.class})
class NebelgastBeguilerTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: Tap target creature")
    void tapsTargetCreatureAndSource() {
        Permanent beguiler = addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(beguiler.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        addReadyBeguiler(player1);
        Permanent artifact = new Permanent(new AngelsFeather());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutMana() {
        addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyBeguiler(Player player) {
        return addCreatureReady(player, new NebelgastBeguiler());
    }
}
