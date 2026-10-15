package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturesClaim;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({VastwoodZendikon.class, Plains.class, DoomBlade.class, GrizzlyBears.class, NaturesClaim.class})
class VastwoodZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 6/4 green Elemental creature and remains a land")
    void enchantedLandBecomesElementalCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.GREEN);
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
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(plainsCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(plainsCard.getId()));
    }

    @Test
    @DisplayName("Vastwood Zendikon can enchant only a land")
    void cannotEnchantNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VastwoodZendikon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent aura = new Permanent(new VastwoodZendikon());
        aura.setAttachedTo(plains.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return plains;
    }

    @Test
    @DisplayName("Resolving the Aura animates an opponent's land and returns it to that opponent on death")
    void opponentsLandReturnsToItsOwner() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Card landCard = plains.getCard();
        harness.setHand(player1, List.of(new VastwoodZendikon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vastwood Zendikon").getAttachedTo()).isEqualTo(plains.getId());
        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(4);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, plains.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(landCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(landCard);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(landCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(landCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(landCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof VastwoodZendikon);
    }

    @Test
    @DisplayName("Destroying the Aura ends animation without returning the land to hand")
    void destroyingAuraLeavesAnOrdinaryLand() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Vastwood Zendikon");
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains).doesNotContain(aura);
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
    }
}
