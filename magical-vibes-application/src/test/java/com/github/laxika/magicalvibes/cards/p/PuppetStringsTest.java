package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PuppetStringsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving taps an untapped target creature")
    void resolvingTapsUntappedCreature() {
        addStrings(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addStringsMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving untaps a tapped target creature")
    void resolvingUntapsTappedCreature() {
        addStrings(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        addStringsMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating taps Puppet Strings and consumes the mana")
    void activatingTapsSourceAndSpendsMana() {
        Permanent strings = addStrings(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addStringsMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(strings.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addStrings(player1);
        Permanent land = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(land);
        addStringsMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        addStrings(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addStringsMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private void addStringsMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addStrings(Player player) {
        Permanent perm = new Permanent(new PuppetStrings());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
