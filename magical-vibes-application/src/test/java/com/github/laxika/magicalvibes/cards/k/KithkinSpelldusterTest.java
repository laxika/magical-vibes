package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.l.LightFromWithin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KithkinSpellduster.class, LightFromWithin.class, DuskdaleWurm.class})
class KithkinSpelldusterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target enchantment when it resolves")
    void resolvingDestroysEnchantment() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Light from Within");
        harness.assertInGraveyard(player2, "Light from Within");
    }

    @Test
    @DisplayName("Persist returns the sacrificed Spellduster with a -1/-1 counter")
    void persistReturnsSpellduster() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Kithkin Spellduster");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Kithkin Spellduster");
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        addReadyDuster(player1);
        Permanent target = addEnchantment(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Light from Within");
    }

    @Test
    @DisplayName("Persist does not return Spellduster if it already had a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent duster = addReadyDuster(player1);
        duster.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kithkin Spellduster");
        harness.assertInGraveyard(player1, "Kithkin Spellduster");
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
                .removeIf(p -> p.getId().equals(target.getId()));

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private Permanent addReadyDuster(Player player) {
        return addCreatureReady(player, new KithkinSpellduster());
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LightFromWithin());
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new DuskdaleWurm());
    }
}
