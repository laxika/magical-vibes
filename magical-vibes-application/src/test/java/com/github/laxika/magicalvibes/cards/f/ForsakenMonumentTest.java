package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForsakenMonument.class, GrizzlyBears.class, SolRing.class, Wastes.class})
class ForsakenMonumentTest extends BaseCardTest {

    @Test
    void boostsOnlyColorlessCreatures() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1,
                colorlessCreature("Colorless creature", 2, 3));
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coloredCreature)).isEqualTo(2);
    }

    @Test
    void tappingWastesAddsOneAdditionalColorlessMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new Wastes());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void tappingSolRingAddsOnlyOneAdditionalMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new SolRing());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void colorlessSpellGainsTwoLife() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void coloredSpellDoesNotGainLife() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private static Card colorlessCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
