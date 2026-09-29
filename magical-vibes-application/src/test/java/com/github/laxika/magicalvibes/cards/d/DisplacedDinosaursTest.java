package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisplacedDinosaurs.class, Millstone.class, GrizzlyBears.class})
class DisplacedDinosaursTest extends BaseCardTest {

    @Test
    @DisplayName("Historic permanents enter as 7/7 Dinosaur creatures while retaining their other types")
    void historicPermanentsEnterAsDinosaurs() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, millstone)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Only historic permanents entering under its controller's control are affected")
    void nonHistoricAndOpponentPermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.enterBattlefieldAndReturn(player2, new Millstone());

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
    }

    @Test
    @DisplayName("The effect only applies as a historic permanent enters")
    void existingHistoricPermanentsAreUnaffected() {
        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());
        assertThat(gqs.isCreature(gd, millstone)).isFalse();

        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        assertThat(gqs.isCreature(gd, millstone)).isFalse();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(0);
    }
}
