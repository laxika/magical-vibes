package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.n.NaturesClaim;
import com.github.laxika.magicalvibes.cards.p.PerimeterCaptain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CorruptedZendikon.class, Plains.class, Smother.class, NaturesClaim.class, PerimeterCaptain.class, ImprisonedInTheMoon.class, TectonicEdge.class})
class CorruptedZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 3/3 black Ooze creature and remains a land")
    void enchantedLandBecomesOozeCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.BLACK);
        assertThat(gqs.computeStaticBonus(gd, plains).grantedSubtypes()).contains(CardSubtype.OOZE);
    }

    @Test
    @DisplayName("When enchanted land dies, it returns to its owner's hand")
    void enchantedLandReturnsToHandWhenDestroyed() {
        Permanent plains = addEnchantedPlains();
        Card plainsCard = plains.getCard();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, plains.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
    }

    @Test
    @DisplayName("Corrupted Zendikon resolves attached to a targeted land")
    void resolvesAttachedToLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new CorruptedZendikon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Corrupted Zendikon").getAttachedTo()).isEqualTo(plains.getId());
        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(3);
    }

    @Test
    @DisplayName("Corrupted Zendikon cannot target a nonland creature")
    void cannotEnchantNonland() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new PerimeterCaptain());
        harness.setHand(player1, List.of(new CorruptedZendikon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, captain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The land stays in the graveyard until the return trigger resolves")
    void returnUsesTheStackAndLeavesAuraInGraveyard() {
        Permanent plains = addEnchantedPlains();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, plains.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plains");
        harness.assertNotInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Corrupted Zendikon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Corrupted Zendikon");
        harness.assertNotInHand(player1, "Corrupted Zendikon");
    }

    @Test
    @DisplayName("Enchanting an opponent's land does not change who receives it when it dies")
    void opponentsLandReturnsToOpponent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Card plainsCard = plains.getCard();
        harness.setHand(player1, List.of(new CorruptedZendikon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, plains.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
        harness.assertInGraveyard(player1, "Corrupted Zendikon");
    }

    @Test
    @DisplayName("Destroying the Aura ends animation without returning the land")
    void destroyingAuraLeavesUnanimatedLandOnBattlefield() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Corrupted Zendikon");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NaturesClaim()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Corrupted Zendikon");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, plains)).isEmpty();
    }

    @Test
    @DisplayName("A later Imprisoned in the Moon makes the enchanted land stop being a creature")
    void laterTypeReplacementEndsCreatureStatus() {
        Permanent land = addLandWithZendikonAndLaterMoon();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    @DisplayName("Destroying the enchanted land while it is not a creature does not trigger a return")
    void noncreatureLandDestructionDoesNotReturnIt() {
        Permanent land = addLandWithZendikonAndLaterMoon();
        Card landCard = land.getCard();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new TectonicEdge());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(landCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(landCard.getId()));
    }

    private Permanent addLandWithZendikonAndLaterMoon() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TectonicEdge());
        harness.setHand(player1, List.of(new CorruptedZendikon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        return land;
    }

    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new CorruptedZendikon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();
        return plains;
    }
}
