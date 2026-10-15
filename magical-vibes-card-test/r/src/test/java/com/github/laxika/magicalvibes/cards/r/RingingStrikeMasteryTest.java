package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.d.DragonstormGlobe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RingingStrikeMastery.class, DragonstormGlobe.class, DalkovanPackbeasts.class})
class RingingStrikeMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Ringing Strike Mastery taps the enchanted creature when it enters")
    void tapsEnchantedCreatureOnEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.setSummoningSick(false);

        harness.setHand(player1, List.of(new RingingStrikeMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ringing Strike Mastery prevents the enchanted creature from untapping")
    void preventsEnchantedCreatureFromUntapping() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.setSummoningSick(false);
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can pay {5} to untap itself")
    void enchantedCreatureCanUntapItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.setSummoningSick(false);
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature loses the granted ability when Ringing Strike Mastery leaves")
    void grantedAbilityStopsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Ringing Strike Mastery cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonstormGlobe());

        harness.setHand(player1, List.of(new RingingStrikeMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The entry trigger taps the currently enchanted creature after the Aura moves")
    void entryTriggerFollowsCurrentAttachment() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        harness.setHand(player1, List.of(new RingingStrikeMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        aura.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(original.isTapped()).isFalse();
        assertThat(replacement.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opposing creature's controller can activate the granted untap ability")
    void opposingControllerCanUntapSummoningSickCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        creature.setSummoningSick(true);
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Four mana is insufficient to activate the granted untap ability")
    void untapAbilityRequiresFiveMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the Aura does not stop an already activated untap ability")
    void activatedUntapSurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DalkovanPackbeasts());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature stays tapped and the lock ends when the Aura leaves")
    void untapLockOnlyAffectsEnchantedCreatureWhileAttached() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        creature.tap();
        other.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RingingStrikeMastery());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature is a legal Aura target")
    void canEnchantAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        creature.tap();
        harness.setHand(player1, List.of(new RingingStrikeMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(aura -> assertThat(aura.getAttachedTo()).isEqualTo(creature.getId()));
    }
}
