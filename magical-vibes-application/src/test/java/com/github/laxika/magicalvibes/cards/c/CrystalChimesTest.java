package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrystalChimesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all enchantment cards from its controller's graveyard to hand")
    void returnsAllEnchantmentCardsFromOwnGraveyard() {
        addReadyChimes(player1);
        Card aura = new AuraOfSilence();
        Card anthem = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(aura, creature, anthem));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aura of Silence");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Crystal Chimes");
        harness.assertNotOnBattlefield(player1, "Crystal Chimes");
    }

    @Test
    @DisplayName("Does not return non-enchantment or opponent graveyard cards")
    void onlyReturnsOwnEnchantments() {
        addReadyChimes(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AuraOfSilence()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Aura of Silence");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
    }

    @Test
    @DisplayName("Requires three mana to activate")
    void requiresThreeMana() {
        addReadyChimes(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChimes(Player player) {
        Permanent chimes = new Permanent(new CrystalChimes());
        chimes.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(chimes);
        return chimes;
    }
}
