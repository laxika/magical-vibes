package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZombieMasterTest extends BaseCardTest {

    // ===== Swampwalk grant =====

    @Test
    @DisplayName("Other Zombie creatures have swampwalk")
    void grantsSwampwalkToOtherZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new ZombieMaster());

        Permanent zombie = zombieNamed(player1, "Gravedigger");

        assertThat(gqs.hasKeyword(gd, zombie, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Zombie Master does not give itself swampwalk")
    void doesNotGrantSwampwalkToItself() {
        harness.addToBattlefield(player1, new ZombieMaster());

        Permanent master = zombieNamed(player1, "Zombie Master");

        assertThat(gqs.hasKeyword(gd, master, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Non-Zombie creatures do not gain swampwalk")
    void doesNotGrantSwampwalkToNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ZombieMaster());

        Permanent bears = zombieNamed(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Zombie creatures gain swampwalk too")
    void grantsSwampwalkToOpponentZombies() {
        harness.addToBattlefield(player1, new ZombieMaster());
        harness.addToBattlefield(player2, new Gravedigger());

        Permanent opponentZombie = zombieNamed(player2, "Gravedigger");

        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Swampwalk is lost when Zombie Master leaves the battlefield")
    void swampwalkLostWhenMasterLeaves() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new ZombieMaster());

        Permanent zombie = zombieNamed(player1, "Gravedigger");
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.SWAMPWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Zombie Master"));

        assertThat(gqs.hasKeyword(gd, zombie, Keyword.SWAMPWALK)).isFalse();
    }

    // ===== Granted regenerate ability =====

    @Test
    @DisplayName("Other Zombies gain \"{B}: Regenerate this permanent.\"")
    void grantsRegenerateAbilityToOtherZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new ZombieMaster());
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent zombie = zombieNamed(player1, "Gravedigger");
        zombie.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);

        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability is lost when Zombie Master leaves the battlefield")
    void regenerateAbilityLostWhenMasterLeaves() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new ZombieMaster());

        Permanent zombie = zombieNamed(player1, "Gravedigger");
        assertThat(gs.getEffectiveActivatedAbilities(gd, zombie)).isNotEmpty();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Zombie Master"));

        assertThat(gs.getEffectiveActivatedAbilities(gd, zombie)).isEmpty();
    }

    // ===== Helpers =====

    private Permanent zombieNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
