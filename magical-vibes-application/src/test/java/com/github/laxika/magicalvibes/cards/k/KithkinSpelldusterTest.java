package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KithkinSpelldusterTest extends BaseCardTest {

    /** Resolves the stack (ability + Persist trigger) until it empties or the game pauses for input. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Ability destroys target enchantment when it resolves")
    void resolvingDestroysEnchantment() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Persist returns the sacrificed Spellduster with a -1/-1 counter")
    void persistReturnsSpellduster() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveUntilInputOrEmpty();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Spellduster"))
                .findFirst().orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Kithkin Spellduster"));
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyDuster(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target enchantment is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Glorious Anthem"));

        resolveUntilInputOrEmpty();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyDuster(Player player) {
        KithkinSpellduster card = new KithkinSpellduster();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEnchantment(Player player) {
        GloriousAnthem card = new GloriousAnthem();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
