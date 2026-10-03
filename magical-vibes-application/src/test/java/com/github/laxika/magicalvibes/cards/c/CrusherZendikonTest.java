package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CrusherZendikon.class, Plains.class, DoomBlade.class, GrizzlyBears.class, NaturesClaim.class})
class CrusherZendikonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 4/2 red Beast creature with trample and remains a land")
    void enchantedLandBecomesBeastCreature() {
        Permanent plains = addEnchantedPlains();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plains)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, plains)).containsExactly(CardColor.RED);
        assertThat(gqs.hasKeyword(gd, plains, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, plains).grantedSubtypes()).contains(CardSubtype.BEAST);
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
    @DisplayName("Crusher Zendikon can enchant only a land")
    void cannotEnchantNonLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new CrusherZendikon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("An opponent's enchanted land returns to its owner after the death trigger resolves")
    void opponentLandReturnsToItsOwner() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new CrusherZendikon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, plains.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plains)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plains)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, plains, Keyword.TRAMPLE)).isTrue();
        assertThat(findPermanent(player1, "Crusher Zendikon").getAttachedTo()).isEqualTo(plains.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, plains.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(plains.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(plains.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(plains.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(plains.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CrusherZendikon);
    }

    @Test
    @DisplayName("Destroying Crusher Zendikon ends animation without returning the surviving land")
    void destroyingAuraLeavesLandOnBattlefield() {
        Permanent plains = addEnchantedPlains();
        Permanent aura = findPermanent(player1, "Crusher Zendikon");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NaturesClaim()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains).doesNotContain(aura);
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
    }

    private Permanent addEnchantedPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CrusherZendikon());
        aura.setAttachedTo(plains.getId());
        return plains;
    }
}
