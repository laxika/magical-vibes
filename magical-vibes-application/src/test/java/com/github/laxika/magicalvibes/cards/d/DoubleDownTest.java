package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubleDown.class})
class DoubleDownTest extends BaseCardTest {

    @ParameterizedTest(name = "copies an outlaw {0} creature spell as a token")
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Copies every outlaw creature subtype as a token")
    void copiesOutlawCreatureAsToken(CardSubtype outlawSubtype) {
        harness.addToBattlefield(player1, new DoubleDown());
        harness.setHand(player1, List.of(outlawCreature(outlawSubtype)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Outlaw Creature"))
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies outlaw instant spells")
    void copiesOutlawInstant() {
        harness.addToBattlefield(player1, new DoubleDown());
        harness.setHand(player1, List.of(outlawInstant(CardSubtype.PIRATE)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not copy a spell without an outlaw subtype")
    void doesNotCopyNonOutlawSpell() {
        harness.addToBattlefield(player1, new DoubleDown());
        harness.setHand(player1, List.of(outlawInstant(null)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(gd.stack).isEmpty();
    }

    private static Card outlawCreature(CardSubtype subtype) {
        Card card = new Card();
        card.setName("Outlaw Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setSubtypes(subtype == null ? List.of() : List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card outlawInstant(CardSubtype subtype) {
        Card card = new Card();
        card.setName("Outlaw Instant");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setSubtypes(subtype == null ? List.of() : List.of(subtype));
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
