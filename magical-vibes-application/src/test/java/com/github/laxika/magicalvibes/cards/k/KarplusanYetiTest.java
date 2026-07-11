package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarplusanYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Fight: 3/3 Yeti kills a 2/2 and survives with marked damage")
    void fightKillsSmallerCreature() {
        Permanent yeti = addReadyYeti(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Bears takes 3 (lethal) and is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        // Yeti takes 2 and survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(yeti.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight: both die when they deal mutual lethal damage")
    void fightMutualLethal() {
        Permanent yeti = addReadyYeti(player1);
        Permanent hillGiant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        harness.activateAbility(player1, 0, null, hillGiant.getId());
        harness.passBothPriorities();

        // Both 3/3s take 3 damage — both destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(hillGiant.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyYeti(player1);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while tapped")
    void cannotActivateWhileTapped() {
        addReadyYeti(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(other);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyYeti(Player player) {
        Permanent perm = new Permanent(new KarplusanYeti());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
