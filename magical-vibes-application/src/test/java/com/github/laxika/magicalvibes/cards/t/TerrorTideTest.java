package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrorTide.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class TerrorTideTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -X/-X for each permanent card in the caster's graveyard")
    void debuffsAllCreaturesByPermanentCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent enemyGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTerrorTide();

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyGiant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count nonpermanent cards in the caster's graveyard")
    void nonpermanentCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Permanent enemyGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTerrorTide();

        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enemyGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures with toughness zero or less die")
    void killsCreaturesWithZeroToughness() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTerrorTide();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent enemyGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTerrorTide();
        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyGiant)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enemyGiant)).isEqualTo(3);
    }

    private void castTerrorTide() {
        harness.setHand(player1, List.of(new TerrorTide()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
    }
}
