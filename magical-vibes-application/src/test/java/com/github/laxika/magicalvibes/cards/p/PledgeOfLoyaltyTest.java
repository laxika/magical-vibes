package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkestHour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PledgeOfLoyaltyTest extends BaseCardTest {

    private static Card coloredPermanent(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Enchanted creature has protection from colors of permanents controlled by the Aura controller")
    void grantsProtectionFromAuraControllerColors() {
        Permanent enchanted = addCreatureReady(player2, coloredPermanent("Enchanted Creature", CardColor.GREEN));
        harness.addToBattlefield(player1, coloredPermanent("Red Permanent", CardColor.RED));
        harness.addToBattlefield(player2, coloredPermanent("Green Permanent", CardColor.GREEN));
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.GREEN)).isFalse();

        Permanent pledge = findPermanent(player1, "Pledge of Loyalty");
        assertThat(pledge.isAttached()).isTrue();
        assertThat(pledge.getAttachedTo()).isEqualTo(enchanted.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Red Permanent"));
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Uses layer-5 colors without recursing through the granted protection")
    void handlesEnchantedPermanentAmongAuraControllersPermanents() {
        Permanent enchanted = addCreatureReady(player1, coloredPermanent("Enchanted Creature", CardColor.WHITE));
        harness.addToBattlefield(player1, new DarkestHour());
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, enchanted)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchanted, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        artifactCard.setColor(CardColor.WHITE);
        harness.addToBattlefield(player1, artifactCard);
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Artifact");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
