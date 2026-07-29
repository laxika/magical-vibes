package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and takes damage equal to its mana value")
    void destroysArtifactAndTakesManaValueDamage() {
        Permanent tinkerer = addReadyTinkerer(player1);
        harness.addToBattlefield(player2, new JalumTome()); // {3}, mana value 3
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Jalum Tome");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jalum Tome");
        // 3 damage to a 1/2 is lethal
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tinkerer);
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
    }

    @Test
    @DisplayName("Survives when the destroyed artifact's mana value is 0")
    void survivesZeroManaValueArtifact() {
        Permanent tinkerer = addReadyTinkerer(player1);
        harness.addToBattlefield(player2, new Ornithopter()); // mana value 0
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tinkerer);
        assertThat(tinkerer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addReadyTinkerer(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTinkerer(Player player) {
        Permanent perm = new Permanent(new GoblinTinkerer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
