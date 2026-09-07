package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieMaster.class, Gravedigger.class, GrizzlyBears.class, Swamp.class})
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

    @Test
    @DisplayName("A Zombie with swampwalk cannot be blocked when the defending player controls a Swamp")
    void swampwalkPreventsBlockingWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player1, new ZombieMaster());
        Permanent attacker = addCreatureReady(player1, new Gravedigger());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Zombie with swampwalk can be blocked when the defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutSwamp() {
        harness.addToBattlefield(player1, new ZombieMaster());
        Permanent attacker = addCreatureReady(player1, new Gravedigger());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Granted regenerate ability =====

    @Test
    @DisplayName("Other Zombies gain \"{B}: Regenerate this permanent.\"")
    void grantsRegenerateAbilityToOtherZombies() {
        Permanent zombie = addCreatureReady(player1, new Gravedigger());
        harness.addToBattlefield(player1, new ZombieMaster());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);

        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only other Zombies gain the regenerate ability")
    void grantsRegenerateAbilityOnlyToOtherZombies() {
        Permanent zombie = addCreatureReady(player1, new Gravedigger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent master = addCreatureReady(player1, new ZombieMaster());

        assertThat(gs.getEffectiveActivatedAbilities(gd, zombie)).isNotEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, master)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Zombie can activate the granted regenerate ability")
    void opponentZombieCanActivateRegenerateAbility() {
        harness.addToBattlefield(player1, new ZombieMaster());
        Permanent zombie = addCreatureReady(player2, new Gravedigger());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(zombie), 0, null, null);
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature Zombie permanent gains the granted regenerate ability")
    void noncreatureZombiePermanentGainsRegenerateAbility() {
        harness.addToBattlefield(player1, new ZombieMaster());
        Permanent zombiePermanent = harness.addToBattlefieldAndReturn(player2, new Swamp());
        zombiePermanent.getGrantedSubtypes().add(CardSubtype.ZOMBIE);

        assertThat(gqs.hasKeyword(gd, zombiePermanent, Keyword.SWAMPWALK)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, zombiePermanent)).isNotEmpty();
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

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    // ===== Helpers =====

    private Permanent zombieNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return findPermanent(player, name);
    }
}
