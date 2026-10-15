package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.cards.r.RayOfRevelation;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurdenOfGuilt.class, SanctuaryCat.class, RayOfRevelation.class})
class BurdenOfGuiltTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Burden of Guilt puts it on the stack")
    void castingPutsOnStack() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        harness.setHand(player1, List.of(new BurdenOfGuilt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creaturePerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Burden of Guilt attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        harness.setHand(player1, List.of(new BurdenOfGuilt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creaturePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burden of Guilt")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creaturePerm.getId()));
    }

    @Test
    @DisplayName("Activating ability taps the enchanted creature")
    void activatingAbilityTapsEnchantedCreature() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // The Aura is at index 1 (creature at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability can be activated multiple times per turn")
    void abilityCanBeActivatedMultipleTimes() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Activate first time
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.isTapped()).isTrue();

        // Activate again (creature already tapped, but ability still works - it just doesn't change state)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can enchant and tap opponent's creature")
    void canEnchantAndTapOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new SanctuaryCat());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        auraPerm.setAttachedTo(opponentCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura is at index 0 on player1's battlefield (only permanent)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability is no longer available when aura is removed from battlefield")
    void abilityGoneWhenAuraRemoved() {
        Permanent creaturePerm = addCreatureReady(player1, new SanctuaryCat());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        auraPerm.setAttachedTo(creaturePerm.getId());

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // The creature should not be tappable via the ability anymore
        // The creature has no activated ability of its own
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creaturePerm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop the creature from being tapped")
    void abilityResolvesAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new SanctuaryCat());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new RayOfRevelation()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(creature.isTapped()).isFalse();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burden of Guilt");
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating requires paying one mana")
    void cannotActivateWithoutMana() {
        Permanent creature = addCreatureReady(player2, new SanctuaryCat());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Aura can activate and only its enchanted creature is tapped")
    void tappedAuraCanActivateWithoutTargetingAnotherCreature() {
        Permanent enchanted = addCreatureReady(player2, new SanctuaryCat());
        Permanent other = addCreatureReady(player2, new SanctuaryCat());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurdenOfGuilt());
        aura.setAttachedTo(enchanted.getId());
        aura.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(enchanted.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        assertThat(aura.isTapped()).isTrue();
    }
}
