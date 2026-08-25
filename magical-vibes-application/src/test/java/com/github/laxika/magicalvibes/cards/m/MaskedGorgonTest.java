package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MaskedGorgon.class)
class MaskedGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Green and white creatures have protection from Gorgons")
    void grantsProtectionFromGorgonsToGreenAndWhiteCreatures() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent greenCreature = addPermanent(player1, CardType.CREATURE, CardColor.GREEN);
        Permanent whiteCreature = addPermanent(player2, CardType.CREATURE, CardColor.WHITE);
        Permanent blueCreature = addPermanent(player1, CardType.CREATURE, CardColor.BLUE);
        Permanent greenEnchantment = addPermanent(player1, CardType.ENCHANTMENT, CardColor.GREEN);
        Permanent gorgon = addPermanent(player2, CardType.CREATURE, CardColor.BLACK, CardSubtype.GORGON);

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, whiteCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, blueCreature, gorgon)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenEnchantment, gorgon)).isFalse();
    }

    @Test
    @DisplayName("Threshold grants protection from green and white")
    void thresholdGrantsProtectionFromGreenAndWhite() {
        Permanent maskedGorgon = addMaskedGorgon(player1);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();

        fillGraveyard(player1, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("The threshold only counts the controller's graveyard")
    void thresholdDoesNotCountOpponentsGraveyard() {
        Permanent maskedGorgon = addMaskedGorgon(player1);
        fillGraveyard(player2, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
    }

    private Permanent addMaskedGorgon(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MaskedGorgon());
    }

    private Permanent addPermanent(Player player, CardType type, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName("Test Permanent");
        card.setType(type);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtypes));

        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Card());
        }
        harness.setGraveyard(player, cards);
    }
}
