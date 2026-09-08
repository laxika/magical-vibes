package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.ThranForge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomWings.class, PhantomWarrior.class, ThranForge.class})
class PhantomWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Phantom Wings attaches it and grants flying")
    void resolvingAttachesAndGrantsFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        harness.setHand(player1, List.of(new PhantomWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phantom Wings")
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Other creatures do not gain flying")
    void doesNotGrantFlyingToOthers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Phantom Wings returns the enchanted creature to its owner's hand")
    void sacrificingBouncesEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.assertOnBattlefield(player1, "Phantom Warrior");
        harness.assertNotOnBattlefield(player1, "Phantom Wings");
        harness.assertInGraveyard(player1, "Phantom Wings");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Phantom Warrior");
        harness.assertNotOnBattlefield(player1, "Phantom Warrior");
        harness.assertNotOnBattlefield(player1, "Phantom Wings");
        harness.assertInGraveyard(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("The creature returns to its owner's hand, not the Aura controller's")
    void returnsToOwnerHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PhantomWarrior());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Phantom Warrior");
        harness.assertNotOnBattlefield(player2, "Phantom Warrior");
        harness.assertInGraveyard(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new PhantomWarrior());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranForge());
        harness.setHand(player1, List.of(new PhantomWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
