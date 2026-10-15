package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({CartoucheOfStrength.class, GrizzlyBears.class, HillGiant.class, Naturalize.class, Unsummon.class})
class CartoucheOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability has the enchanted creature fight target creature an opponent controls")
    void acceptingFightsWithEnchantedCreature() {
        Permanent myGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CartoucheOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Enchanted 3/3 → 4/4 deals 4 to the opponent's 2/2, which dies; the 2/2 deals 2 back to the
        // 4/4, which survives with 2 marked damage. If the Aura (power 0) fought, the 2/2 would live.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentBears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(myGiant.getId()));
        assertThat(myGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability does not fight")
    void decliningDoesNotFight() {
        Permanent myGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CartoucheOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentBears.getId()));
        assertThat(opponentBears.getMarkedDamage()).isZero();
        assertThat(myGiant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has trample")
    void enchantedCreatureBoostedAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfStrength());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and trample when the Cartouche is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfStrength());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CartoucheOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The creature still fights without the bonus if the Aura is destroyed in response")
    void fightsAfterAuraIsDestroyed() {
        Permanent myGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CartoucheOfStrength(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentGiant.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Cartouche of Strength"));
        harness.assertInGraveyard(player1, "Cartouche of Strength");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The Aura resolves without an opposing creature to target")
    void resolvesWithoutOpposingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CartoucheOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cartouche of Strength");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The boost applies during the fight and trample does not damage the opponent")
    void boostAppliesDuringFightWithoutTrampleDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CartoucheOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Cartouche of Strength");
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("No fight occurs if the opposing target leaves before the trigger resolves")
    void targetLeavesBeforeFight() {
        Permanent myGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CartoucheOfStrength(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(myGiant.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cartouche of Strength");
    }

    @Test
    @DisplayName("No fight occurs if the enchanted creature leaves before resolution")
    void enchantedCreatureLeavesBeforeFight() {
        Permanent myGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CartoucheOfStrength(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.castAndResolveInstant(player1, 0, myGiant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Cartouche of Strength");
    }
}
