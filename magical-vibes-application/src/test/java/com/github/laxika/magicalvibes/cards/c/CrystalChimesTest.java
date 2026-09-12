package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Attunement;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalChimes.class, Attunement.class, GloriousAnthem.class, GorillaWarrior.class})
class CrystalChimesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all enchantment cards from its controller's graveyard to hand")
    void returnsAllEnchantmentCardsFromOwnGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new CrystalChimes());
        Card enchantment = new Attunement();
        Card anthem = new GloriousAnthem();
        Card creature = new GorillaWarrior();
        harness.setGraveyard(player1, List.of(enchantment, creature, anthem));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Attunement");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Crystal Chimes");
        harness.assertNotOnBattlefield(player1, "Crystal Chimes");
    }

    @Test
    @DisplayName("Does not return non-enchantment or opponent graveyard cards")
    void onlyReturnsOwnEnchantments() {
        harness.addToBattlefieldAndReturn(player1, new CrystalChimes());
        harness.setGraveyard(player1, List.of(new GorillaWarrior()));
        harness.setGraveyard(player2, List.of(new Attunement()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player2, "Attunement");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
    }

    @Test
    @DisplayName("Requires three mana to activate")
    void requiresThreeMana() {
        harness.addToBattlefieldAndReturn(player1, new CrystalChimes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires an untapped source to activate")
    void requiresUntappedSource() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new CrystalChimes());
        chimes.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
