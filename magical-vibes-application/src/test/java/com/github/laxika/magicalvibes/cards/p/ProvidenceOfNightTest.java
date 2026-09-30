package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ProvidenceOfNight.class)
class ProvidenceOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell with hybrid mana in its mana cost")
    void copiesHybridManaSpell() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(lifeGainInstant("{W/U}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not copy a spell without hybrid mana")
    void doesNotCopyNonHybridSpell() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(lifeGainInstant("{1}")));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Has protection from monocolored sources")
    void hasProtectionFromMonocoloredSources() {
        Permanent providence = harness.addToBattlefieldAndReturn(player1, new ProvidenceOfNight());
        Permanent monocoloredSource = coloredSource(List.of(CardColor.RED));
        Permanent multicoloredSource = coloredSource(List.of(CardColor.RED, CardColor.BLUE));

        assertThat(gqs.hasProtectionFromSource(gd, providence, monocoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, providence, multicoloredSource)).isFalse();
    }

    private static Permanent coloredSource(List<CardColor> colors) {
        Card card = new Card();
        card.setColors(colors);
        return new Permanent(card);
    }

    private static Card lifeGainInstant(String manaCost) {
        Card card = new Card();
        card.setName("Test Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
