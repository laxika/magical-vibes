package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FeedTheSwarm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NissasZendikon.class, DoomBlade.class, GrizzlyBears.class, Plains.class, FeedTheSwarm.class})
class NissasZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 4/4 Elemental creature with reach and haste and remains a land")
    void enchantedLandBecomesElementalCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, plains)).isEmpty();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.HASTE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, plains).grantedSubtypes()).contains(CardSubtype.ELEMENTAL);
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
    @DisplayName("Nissa's Zendikon can enchant only a land")
    void cannotEnchantNonLand() {
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NissasZendikon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving the Aura animates a land without changing its color or controller")
    void auraCanEnchantOpponentsLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new NissasZendikon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nissa's Zendikon").getAttachedTo()).isEqualTo(plains.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, plains)).isEmpty();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Destroying the Aura ends animation without returning the land")
    void destroyingAuraLeavesLandOnBattlefield() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Nissa's Zendikon");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FeedTheSwarm()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Nissa's Zendikon");
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("An opponent's enchanted land returns to its owner, not the Aura controller")
    void opponentsLandReturnsToOwnerAfterDeath() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new NissasZendikon(), new FeedTheSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, plains.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(plains.getCard());
        harness.assertNotInHand(player2, "Plains");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(plains.getCard());
        harness.assertNotInHand(player1, "Plains");
        harness.assertNotInGraveyard(player2, "Plains");
        harness.assertInGraveyard(player1, "Nissa's Zendikon");
    }

    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NissasZendikon());
        aura.setAttachedTo(plains.getId());
        return plains;
    }
}
