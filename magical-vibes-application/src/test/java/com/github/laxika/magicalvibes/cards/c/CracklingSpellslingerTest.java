package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElementalEruption;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CracklingSpellslinger.class, ElementalEruption.class, SolRing.class})
class CracklingSpellslingerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, gives the next instant or sorcery storm")
    void castGivesNextInstantOrSorceryStorm() {
        castSpellslinger();

        harness.setHand(player1, List.of(lifeGainInstant(), lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not give storm when put onto the battlefield")
    void putOntoBattlefieldDoesNotGiveStorm() {
        harness.enterBattlefieldAndReturn(player1, new CracklingSpellslinger());

        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    private void castSpellslinger() {
        harness.castFromHand(player1, new CracklingSpellslinger(), "{3}{R}{R}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Granted storm triggers separately from a spell's own storm")
    void addsStormToSpellThatAlreadyHasStorm() {
        castSpellslinger();
        harness.castFromHand(player1, new ElementalEruption(), "{4}{R}{R}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon Elemental")).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Spellslingers grant separate instances of storm")
    void multipleGrantsApplyToSameSpell() {
        castSpellslinger();
        castSpellslinger();
        harness.castFromHand(player1, new ElementalEruption(), "{4}{R}{R}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon Elemental")).isEqualTo(7);
    }

    @Test
    @DisplayName("An artifact does not consume the grant and counts toward storm")
    void artifactDoesNotConsumeGrant() {
        castSpellslinger();
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();
        harness.castFromHand(player1, new ElementalEruption(), "{4}{R}{R}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon Elemental")).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent spells count toward storm without consuming the grant")
    void opponentSpellDoesNotConsumeGrant() {
        castSpellslinger();
        harness.castFromHand(player2, new CracklingSpellslinger(), "{3}{R}{R}");
        resolveAllTriggers();
        harness.castFromHand(player1, new ElementalEruption(), "{4}{R}{R}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon Elemental")).isEqualTo(5);
        assertThat(countPermanents(player2, "Dragon Elemental")).isZero();
    }

    @Test
    @DisplayName("An unused storm grant expires at the end of the turn")
    void unusedGrantExpires() {
        harness.setHand(player2, List.of());
        castSpellslinger();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();
        harness.castFromHand(player1, new ElementalEruption(), "{4}{R}{R}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon Elemental")).isEqualTo(2);
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
