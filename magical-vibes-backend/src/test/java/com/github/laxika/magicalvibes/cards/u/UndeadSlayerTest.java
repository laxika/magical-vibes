package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UndeadSlayerTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Undead Slayer has a tap + mana activated ability that exiles")
    void hasCorrectAbility() {
        UndeadSlayer card = new UndeadSlayer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{W}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects())
                .hasSize(1)
                .first().isInstanceOf(ExileTargetPermanentEffect.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Exiles target Zombie")
    void exilesTargetZombie() {
        addReadySlayer(player1);
        Permanent zombie = addReadyPermanent(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
    }

    // ===== Target restriction =====

    @Test
    @DisplayName("Cannot target non-Skeleton/Vampire/Zombie creature")
    void cannotTargetNonUndeadCreature() {
        addReadySlayer(player1);
        Permanent bears = addReadyPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new UndeadSlayer());
        Permanent zombie = addReadyPermanent(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadySlayer(player1);
        Permanent zombie = addReadyPermanent(player2, new ScatheZombies());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadySlayer(player1);
        Permanent zombie = addReadyPermanent(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadySlayer(Player player) {
        UndeadSlayer card = new UndeadSlayer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
