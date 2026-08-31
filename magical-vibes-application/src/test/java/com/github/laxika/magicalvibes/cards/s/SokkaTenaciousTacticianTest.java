package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EarthenAlly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkaTenaciousTactician.class, EarthenAlly.class, GrizzlyBears.class, Shock.class})
class SokkaTenaciousTacticianTest extends BaseCardTest {

    @Test
    void otherAlliesGainMenaceAndProwess() {
        Permanent sokka = addCreatureReady(player1, new SokkaTenaciousTactician());
        Permanent ally = addCreatureReady(player1, new EarthenAlly());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAlly = addCreatureReady(player2, new EarthenAlly());
        int sokkaPower = gqs.getEffectivePower(gd, sokka);
        int allyPower = gqs.getEffectivePower(gd, ally);
        int allyToughness = gqs.getEffectiveToughness(gd, ally);
        int nonAllyPower = gqs.getEffectivePower(gd, nonAlly);

        assertThat(gqs.hasKeyword(gd, sokka, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAlly, Keyword.MENACE)).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sokka)).isEqualTo(sokkaPower + 1);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(allyPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(allyToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonAlly)).isEqualTo(nonAllyPower);
    }

    @Test
    void createsAnAllyTokenForEachNoncreatureSpell() {
        addCreatureReady(player1, new SokkaTenaciousTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Ally");
        int tokenPower = gqs.getEffectivePower(gd, token);
        assertThat(tokenPower).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(tokenPower + 1);
    }

    @Test
    void creatureSpellsAndOpponentsSpellsDoNotTriggerSokkaOrOtherAllies() {
        Permanent sokka = addCreatureReady(player1, new SokkaTenaciousTactician());
        Permanent ally = addCreatureReady(player1, new EarthenAlly());
        int sokkaPower = gqs.getEffectivePower(gd, sokka);
        int allyPower = gqs.getEffectivePower(gd, ally);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sokka)).isEqualTo(sokkaPower);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(allyPower);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sokka)).isEqualTo(sokkaPower);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(allyPower);
    }
}
