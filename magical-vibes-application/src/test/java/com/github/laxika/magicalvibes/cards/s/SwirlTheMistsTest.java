package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swirl the Mists rewrites every color word on every object (CR 612.1, layer 3 per CR 613.1c).
 * Paladin en-Vec's printed "protection from black and from red" is the observable probe: under a
 * chosen green, both words become "green".
 */
class SwirlTheMistsTest extends BaseCardTest {

    private Permanent addSwirl(CardColor chosenColor) {
        Permanent swirl = new Permanent(new SwirlTheMists());
        swirl.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(swirl);
        return swirl;
    }

    @Test
    @DisplayName("Every color word in a permanent's text becomes the chosen word")
    void rewritesAllColorWords() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        addSwirl(CardColor.GREEN);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Applies to the controller's own permanents too")
    void rewritesOwnPermanents() {
        harness.addToBattlefield(player1, new PaladinEnVec());
        Permanent paladin = findPermanent(player1, "Paladin en-Vec");
        addSwirl(CardColor.BLUE);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Color words matching the chosen word are untouched")
    void keepsAlreadyChosenWord() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        addSwirl(CardColor.RED);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("No rewriting before a color is chosen")
    void noRewriteWithoutChosenColor() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        harness.addToBattlefield(player1, new SwirlTheMists());

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The rewrite stops when Swirl the Mists leaves the battlefield")
    void rewriteEndsWhenSwirlLeaves() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        Permanent swirl = addSwirl(CardColor.GREEN);
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(swirl);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Subsumes an earlier one-shot text change on the same permanent")
    void subsumesRecordedTextReplacement() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        paladin.getTextReplacements().add(new TextReplacement("black", "white"));
        addSwirl(CardColor.GREEN);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose a color word, all color words change")
    void fullFlow() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        Permanent paladin = findPermanent(player2, "Paladin en-Vec");
        harness.setHand(player1, List.of(new SwirlTheMists()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
    }
}
