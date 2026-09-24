package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArnoDorian.class, AssassinInitiate.class, GrizzlyBears.class})
class ArnoDorianTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other Assassins you control")
    void buffsOtherAssassinsYouControl() {
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        int basePower = gqs.getEffectivePower(gd, assassin);
        int baseToughness = gqs.getEffectiveToughness(gd, assassin);

        addCreatureReady(player1, new ArnoDorian());

        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, assassin)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent arno = addCreatureReady(player1, new ArnoDorian());
        int basePower = gqs.getEffectivePower(gd, arno);
        int baseToughness = gqs.getEffectiveToughness(gd, arno);

        addCreatureReady(player1, new AssassinInitiate());

        assertThat(gqs.getEffectivePower(gd, arno)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, arno)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff non-Assassins or opposing Assassins")
    void doesNotBuffNonAssassinsOrOpposingAssassins() {
        Permanent nonAssassin = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAssassin = addCreatureReady(player2, new AssassinInitiate());
        int nonAssassinPower = gqs.getEffectivePower(gd, nonAssassin);
        int nonAssassinToughness = gqs.getEffectiveToughness(gd, nonAssassin);
        int opposingPower = gqs.getEffectivePower(gd, opposingAssassin);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingAssassin);

        addCreatureReady(player1, new ArnoDorian());

        assertThat(gqs.getEffectivePower(gd, nonAssassin)).isEqualTo(nonAssassinPower);
        assertThat(gqs.getEffectiveToughness(gd, nonAssassin)).isEqualTo(nonAssassinToughness);
        assertThat(gqs.getEffectivePower(gd, opposingAssassin)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingAssassin)).isEqualTo(opposingToughness);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its disguise cost")
    void canBeDisguisedAndTurnedFaceUp() {
        ArnoDorian card = new ArnoDorian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent arno = findPermanentForCard(card);
        assertThat(arno.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(arno));

        assertThat(arno.isFaceDown()).isFalse();
    }

    private Permanent findPermanentForCard(ArnoDorian card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
