package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Befoul;
import com.github.laxika.magicalvibes.cards.h.Hibernation;
import com.github.laxika.magicalvibes.cards.m.MindBend;
import com.github.laxika.magicalvibes.cards.n.NezumiCutthroat;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swirl the Mists rewrites color words on spells and permanents (CR 612.1, layer 3 per CR 613.1c).
 * Paladin en-Vec's printed "protection from black and from red" is the observable probe: under a
 * chosen green, both words become "green".
 */
@CardUsed({SwirlTheMists.class, PaladinEnVec.class, Befoul.class, NezumiCutthroat.class, Swamp.class,
        MindBend.class, Hibernation.class})
class SwirlTheMistsTest extends BaseCardTest {

    private Permanent addSwirl(CardColor chosenColor) {
        Permanent swirl = harness.addToBattlefieldAndReturn(player1, new SwirlTheMists());
        swirl.setChosenColor(chosenColor);
        return swirl;
    }

    @Test
    @DisplayName("Every color word in a permanent's text becomes the chosen word")
    void rewritesAllColorWords() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        addSwirl(CardColor.GREEN);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Applies to the controller's own permanents too")
    void rewritesOwnPermanents() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinEnVec());
        addSwirl(CardColor.BLUE);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Color words matching the chosen word are untouched")
    void keepsAlreadyChosenWord() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        addSwirl(CardColor.RED);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("No rewriting before a color is chosen")
    void noRewriteWithoutChosenColor() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.addToBattlefield(player1, new SwirlTheMists());

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The rewrite stops when Swirl the Mists leaves the battlefield")
    void rewriteEndsWhenSwirlLeaves() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        Permanent swirl = addSwirl(CardColor.GREEN);
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(swirl);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Subsumes an earlier one-shot text change on the same permanent")
    void subsumesRecordedTextReplacement() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        paladin.getTextReplacements().add(new TextReplacement("black", "white"));
        addSwirl(CardColor.GREEN);

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose a color word, all color words change")
    void fullFlow() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
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

    @Test
    @DisplayName("Rewrites a color word in a spell's targeting text")
    void rewritesSpellTargetingText() {
        addSwirl(CardColor.BLUE);
        harness.addToBattlefield(player1, new Swamp());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NezumiCutthroat());
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, blackCreature.getId());

        harness.assertNotOnBattlefield(player2, "Nezumi Cutthroat");
    }

    @Test
    @DisplayName("A later text change can replace the color word supplied by Swirl")
    void laterTextChangeRewritesSwirlColor() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        addSwirl(CardColor.GREEN);
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, paladin.getId());
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The newer Swirl wins even when the older Swirl belongs to the opponent")
    void multipleSwirlsFollowTimestampOrder() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        Permanent older = harness.addToBattlefieldAndReturn(player2, new SwirlTheMists());
        older.setChosenColor(CardColor.RED);
        older.setTimestamp(gd.nextTimestamp());
        Permanent newer = addSwirl(CardColor.GREEN);
        newer.setTimestamp(gd.nextTimestamp());

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Rewrites a color word in a resolving spell's effect")
    void rewritesSpellEffect() {
        addSwirl(CardColor.BLACK);
        harness.addToBattlefield(player2, new NezumiCutthroat());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Nezumi Cutthroat");
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof NezumiCutthroat);
        harness.assertOnBattlefield(player1, "Swirl the Mists");
    }
}
