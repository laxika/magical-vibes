package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.cards.s.SetessanPetitioner;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MantleOfTheWolf.class, AltarOfThePantheon.class, SetessanPetitioner.class, FinalDeath.class, RevokeExistence.class})
class MantleOfTheWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Mantle of the Wolf attaches to a creature and gives it +4/+4")
    void attachesAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SetessanPetitioner());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mantle of the Wolf");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Mantle of the Wolf's boost ends when the Aura is put into a graveyard")
    void boostEndsWhenAuraIsRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SetessanPetitioner());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Mantle of the Wolf is put into a graveyard, it creates two Wolf tokens")
    void createsTwoWolvesWhenPutIntoGraveyard() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(2);
        assertThat(wolves).allSatisfy(wolf -> {
            assertThat(wolf.getCard().isToken()).isTrue();
            assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);
            assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Mantle of the Wolf cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AltarOfThePantheon());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An Aura on an opponent's creature boosts it but gives Wolves to the Aura's controller")
    void enchantingOpponentsCreatureCreatesWolvesForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SetessanPetitioner());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Mantle of the Wolf");
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Exiling Mantle of the Wolf removes its boost without creating Wolves")
    void exilingAuraDoesNotCreateWolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SetessanPetitioner());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, aura.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
        harness.assertNotInGraveyard(player1, "Mantle of the Wolf");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mantle of the Wolf creates no Wolves when its spell's target disappears")
    void illegalTargetOnResolutionDoesNotCreateWolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SetessanPetitioner());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mantle of the Wolf");
        harness.assertNotOnBattlefield(player1, "Mantle of the Wolf");
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Mantle of the Wolf to hand removes its boost without creating Wolves")
    void returningAuraToHandDoesNotCreateWolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SetessanPetitioner());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));

        harness.assertInHand(player1, "Mantle of the Wolf");
        harness.assertNotInGraveyard(player1, "Mantle of the Wolf");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
