package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Firebreathing.class, GrizzlyBears.class, Mountain.class})
class FirebreathingTest extends BaseCardTest {

    private Permanent attachTo(Permanent host) {
        Permanent aura = new Permanent(new Firebreathing());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Firebreathing attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Granted ability =====

    @Test
    @DisplayName("Enchanted creature can activate {R}: +1/+0")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        attachTo(bearsPerm);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two activations stack and expire at end of turn")
    void boostsStackAndWearOffAtEndOfTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        attachTo(bearsPerm);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enchanted creature's controller can activate the granted ability")
    void enchantedCreatureControllerCanActivateGrantedAbility() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        attachTo(bearsPerm);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability disappears when Firebreathing leaves the battlefield")
    void grantedAbilityDisappearsWhenAuraLeavesBattlefield() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachTo(bearsPerm);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability requires one red mana")
    void cannotActivateWithoutRedMana() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        attachTo(bearsPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Firebreathing can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
