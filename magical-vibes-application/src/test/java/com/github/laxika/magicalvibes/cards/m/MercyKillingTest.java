package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercyKillingTest extends BaseCardTest {

    private long elfWarriorCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .count();
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices the target and its controller creates power-many Elf Warrior tokens")
    void sacrificesAndCreatesTokensEqualToPower() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        castAt(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // The target's controller (player2), not the caster, gets the tokens; X = power (2).
        assertThat(elfWarriorCount(player2)).isEqualTo(2);
        assertThat(elfWarriorCount(player1)).isZero();
    }

    @Test
    @DisplayName("A zero-power creature is sacrificed but creates no tokens")
    void zeroPowerCreatesNoTokens() {
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter()); // 0/2

        castAt(ornithopter);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(elfWarriorCount(player2)).isZero();
    }

    @Test
    @DisplayName("Can target and sacrifice the caster's own creature")
    void canTargetOwnCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        castAt(bears);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(elfWarriorCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GrizzlyBears()); // a legal creature exists, so the spell is playable
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
