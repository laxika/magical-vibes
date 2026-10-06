package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SagesReverie.class, AbundantGrowth.class, Forest.class, GrizzlyBears.class,
        UnholyStrength.class})
class SagesReverieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for each Aura attached to a creature, including itself")
    void drawsForEachAuraAttachedToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent existingAura = harness.addToBattlefieldAndReturn(player1, new UnholyStrength());
        existingAura.setAttachedTo(bears.getId());

        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent auraAttachedToNoncreature = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        auraAttachedToNoncreature.setAttachedTo(forest.getId());

        harness.setHand(player1, List.of(new SagesReverie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Boosts the enchanted creature based on the controller's attached Auras")
    void boostsBasedOnControlledAurasAttachedToCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent reverie = harness.addToBattlefieldAndReturn(player1, new SagesReverie());
        reverie.setAttachedTo(bears.getId());

        Permanent existingAura = harness.addToBattlefieldAndReturn(player1, new UnholyStrength());
        existingAura.setAttachedTo(bears.getId());

        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent auraAttachedToNoncreature = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        auraAttachedToNoncreature.setAttachedTo(forest.getId());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new UnholyStrength());
        opponentAura.setAttachedTo(opponentBears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SagesReverie()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counts controlled Auras on opposing creatures and excludes opposing Auras")
    void countsControlledAurasRegardlessOfCreatureController() {
        harness.setHand(player2, List.of());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new UnholyStrength());
        ownAura.setAttachedTo(otherBears.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new UnholyStrength());
        opposingAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new SagesReverie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Draw count uses the Auras still present when the trigger resolves")
    void countsAurasAtTriggerResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnholyStrength());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new SagesReverie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws no cards if its only qualifying Aura leaves before the trigger resolves")
    void drawsZeroAfterReverieLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SagesReverie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent reverie = findPermanent(player1, "Sage's Reverie");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, reverie));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The continuous bonus updates as another creature's Aura leaves")
    void updatesBonusWhenAuraOnAnotherCreatureLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent reverie = harness.addToBattlefieldAndReturn(player1, new SagesReverie());
        reverie.setAttachedTo(bears.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnholyStrength());
        aura.setAttachedTo(otherBears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
