package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CelestialDawnTest extends BaseCardTest {

    @Test
    @DisplayName("A Forest you control taps for white instead of green")
    void ownForestProducesWhite() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent's Mountain is unaffected — still taps for red")
    void opponentLandUnaffected() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.forceActivePlayer(player2);

        gs.tapPermanent(gd, player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("A green creature you control becomes white")
    void ownCreatureBecomesWhite() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("An opponent's green creature keeps its color")
    void opponentCreatureKeepsColor() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A land you control keeps its color identity — only nonland permanents turn white")
    void landIsNotColored() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("White mana pays a red pip — Hill Giant castable off {W}{W}{W}{W}")
    void whitePaysColoredPipsOfAnyColor() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
    }

    @Test
    @DisplayName("Non-white mana can only pay generic — {R}{R}{R}{R} can't cast Hill Giant")
    void otherManaOnlyPaysGeneric() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Non-white mana still pays the generic part alongside white for the pips")
    void otherManaPaysGenericPortion() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
    }
}
