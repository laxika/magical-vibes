package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.n.NaturesClaim;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GuardianZendikon.class, Plains.class, DoomBlade.class, GrizzlyBears.class, NaturesClaim.class})
class GuardianZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 2/6 white Wall creature with defender and remains a land")
    void enchantedLandBecomesWallCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(6);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, plains, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, plains)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("When enchanted land dies, it returns to its owner's hand")
    void enchantedLandReturnsToHandWhenDestroyed() {
        Permanent plains = addEnchantedPlains();
        Card plainsCard = plains.getCard();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
    }

    @Test
    @DisplayName("Guardian Zendikon can enchant only a land")
    void cannotEnchantNonLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new GuardianZendikon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Defender prevents the animated land from attacking")
    void animatedLandCannotAttack() {
        Permanent plains = addEnchantedPlains();
        plains.setSummoningSick(false);

        assertThat(als.canAttack(gd, plains, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Destroying the Aura leaves the land on the battlefield and ends animation")
    void destroyingAuraEndsAnimationWithoutReturningLand() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Guardian Zendikon");
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Guardian Zendikon");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, plains)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land can be enchanted and returns to its owner after dying")
    void opponentsLandReturnsToItsOwnerAfterTriggerResolves() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new GuardianZendikon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Guardian Zendikon").getAttachedTo()).isEqualTo(plains.getId());
        assertThat(gqs.isCreature(gd, plains)).isTrue();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, plains.getId());

        harness.assertInGraveyard(player2, "Plains");
        harness.assertNotInHand(player2, "Plains");
        harness.assertInGraveyard(player1, "Guardian Zendikon");
        resolveAllTriggers();

        harness.assertInHand(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
        harness.assertNotInGraveyard(player2, "Plains");
    }

    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent aura = new Permanent(new GuardianZendikon());
        aura.setAttachedTo(plains.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return plains;
    }
}
