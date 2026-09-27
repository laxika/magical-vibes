package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AnuridBrushhopper;
import com.github.laxika.magicalvibes.cards.e.EpicStruggle;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.l.Lifelace;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.x.XathridGorgon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridBrushhopper.class, EpicStruggle.class, HaplessResearcher.class, IronshellBeetle.class, Lifelace.class, MaskedGorgon.class, SuntailHawk.class, XathridGorgon.class})
class MaskedGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Green and white creatures have protection from Gorgons")
    void grantsProtectionFromGorgonsToGreenAndWhiteCreatures() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent greenWhiteCreature = harness.addToBattlefieldAndReturn(player1, new AnuridBrushhopper());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new HaplessResearcher());
        Permanent greenEnchantment = harness.addToBattlefieldAndReturn(player1, new EpicStruggle());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, whiteCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenWhiteCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, blueCreature, gorgon)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenEnchantment, gorgon)).isFalse();
    }

    @Test
    @DisplayName("Threshold grants protection from green and white")
    void thresholdGrantsProtectionFromGreenAndWhite() {
        Permanent maskedGorgon = addMaskedGorgon(player1);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();

        fillGraveyard(player1, 6);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.BLUE)).isFalse();

        fillGraveyard(player1, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.BLUE)).isFalse();

        fillGraveyard(player1, 6);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("The threshold only counts the controller's graveyard")
    void thresholdDoesNotCountOpponentsGraveyard() {
        Permanent maskedGorgon = addMaskedGorgon(player1);
        fillGraveyard(player2, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("A color-changed Masked Gorgon is included in its global protection effect")
    void colorChangedMaskedGorgonProtectsItselfFromGorgons() {
        Permanent maskedGorgon = addMaskedGorgon(player1);
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());

        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, maskedGorgon.getId());

        assertThat(gqs.getEffectiveColors(gd, maskedGorgon)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, maskedGorgon, gorgon)).isTrue();
    }

    private Permanent addMaskedGorgon(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MaskedGorgon());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new SuntailHawk());
        }
        harness.setGraveyard(player, cards);
    }

    @Test
    @CardUsed(Lifelace.class)
    @DisplayName("A green Masked Gorgon has protection from Gorgons")
    void greenMaskedGorgonHasProtectionFromGorgons() {
        Permanent maskedGorgon = addMaskedGorgon(player1);

        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, maskedGorgon.getId());

        assertThat(gqs.getEffectiveColors(gd, maskedGorgon)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, maskedGorgon, maskedGorgon)).isTrue();
    }
}
