package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KaijinOfTheVanishingTouch;
import com.github.laxika.magicalvibes.cards.o.OrbOfDreams;
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

@CardUsed({PhantomWings.class, KaijinOfTheVanishingTouch.class, OrbOfDreams.class})
class PhantomWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Phantom Wings attaches it and grants flying")
    void resolvingAttachesAndGrantsFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KaijinOfTheVanishingTouch());

        harness.setHand(player1, List.of(new PhantomWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phantom Wings")
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Other creatures do not gain flying")
    void doesNotGrantFlyingToOthers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KaijinOfTheVanishingTouch());

        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new KaijinOfTheVanishingTouch());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Phantom Wings returns the enchanted creature to its owner's hand")
    void sacrificingBouncesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KaijinOfTheVanishingTouch());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.assertOnBattlefield(player1, "Kaijin of the Vanishing Touch");
        harness.assertNotOnBattlefield(player1, "Phantom Wings");
        harness.assertInGraveyard(player1, "Phantom Wings");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Kaijin of the Vanishing Touch");
        harness.assertNotOnBattlefield(player1, "Kaijin of the Vanishing Touch");
        harness.assertNotOnBattlefield(player1, "Phantom Wings");
        harness.assertInGraveyard(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("The creature returns to its owner's hand, not the Aura controller's")
    void returnsToOwnerHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KaijinOfTheVanishingTouch());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kaijin of the Vanishing Touch");
        harness.assertNotOnBattlefield(player2, "Kaijin of the Vanishing Touch");
        harness.assertInGraveyard(player1, "Phantom Wings");
    }

    @Test
    @DisplayName("Sacrificing an unattached Aura does not return a creature")
    void sacrificingUnattachedAuraDoesNotReturnCreature() {
        harness.addToBattlefield(player1, new KaijinOfTheVanishingTouch());
        harness.addToBattlefield(player1, new PhantomWings());

        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Phantom Wings");
        harness.assertInGraveyard(player1, "Phantom Wings");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kaijin of the Vanishing Touch");
        harness.assertNotInHand(player1, "Kaijin of the Vanishing Touch");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OrbOfDreams());
        harness.setHand(player1, List.of(new PhantomWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
