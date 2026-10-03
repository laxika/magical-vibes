package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cactarantula;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.n.NezumiLinkbreaker;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubleDown.class, NezumiLinkbreaker.class, NamelessInversion.class, Cactarantula.class})
class DoubleDownTest extends BaseCardTest {

    @ParameterizedTest(name = "copies an outlaw {0} creature spell as a token")
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Copies every outlaw creature subtype as a token")
    void copiesOutlawCreatureAsToken(CardSubtype outlawSubtype) {
        harness.addToBattlefield(player1, new DoubleDown());
        harness.castFromHand(player1, outlawCreature(outlawSubtype), "{1}");
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
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, outlawInstant(CardSubtype.PIRATE), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not copy a spell without an outlaw subtype")
    void doesNotCopyNonOutlawSpell() {
        harness.addToBattlefield(player1, new DoubleDown());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, outlawInstant(null), "{1}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsOutlawSpell() {
        harness.addToBattlefield(player1, new DoubleDown());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NezumiLinkbreaker(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Nezumi Linkbreaker"))
                .hasSize(1)
                .allMatch(p -> !p.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesKindredChangelingOnceAndPreservesItsTarget() {
        harness.addToBattlefield(player1, new DoubleDown());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cactarantula");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cactarantula");
        harness.assertInGraveyard(player1, "Cactarantula");
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
        if (subtype != null) {
            card.setAdditionalTypes(Set.of(CardType.KINDRED));
        }
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setSubtypes(subtype == null ? List.of() : List.of(subtype));
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
