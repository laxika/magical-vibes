package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AkromasVengeance;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.n.NaturesClaim;
import com.github.laxika.magicalvibes.cards.s.Smother;
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

@CardUsed({WindZendikon.class, Plains.class, DoomBlade.class, GrizzlyBears.class, NaturesClaim.class, Smother.class, AkromasVengeance.class})
class WindZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 2/2 blue Elemental creature with flying and remains a land")
    void enchantedLandBecomesElementalCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, plains, Keyword.FLYING)).isTrue();
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
    @DisplayName("Wind Zendikon can enchant only a land")
    void cannotEnchantNonLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new WindZendikon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Casting Wind Zendikon on an opponent's land returns it to that opponent after death")
    void opponentsLandReturnsToItsOwner() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Card plainsCard = plains.getCard();
        harness.setHand(player1, List.of(new WindZendikon(), new Smother()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, plains.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WindZendikon);
    }

    @Test
    @DisplayName("Destroying Wind Zendikon ends animation without returning the land")
    void removingAuraEndsAnimationWithoutReturningLand() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Wind Zendikon");
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains).doesNotContain(aura);
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.FLYING)).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plains.getCard().getId()));
    }

    @Test
    @DisplayName("Simultaneous destruction of the Aura and land still returns the land")
    void simultaneousAuraAndLandDestructionReturnsLand() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WindZendikon());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        aura.setAttachedTo(plains.getId());
        Card plainsCard = plains.getCard();
        harness.setHand(player1, List.of(new AkromasVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura, plains);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WindZendikon)
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
    }
    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent aura = new Permanent(new WindZendikon());
        aura.setAttachedTo(plains.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return plains;
    }
}
