package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMeep.class, ColossalDreadmaw.class, GrizzlyBears.class, LlanowarElves.class})
class TheMeepTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature sets your creatures' base power and toughness to its mana value")
    void sacrificingAnotherCreatureSetsBasePowerAndToughness() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent sacrificed = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Accepting without another creature does nothing")
    void acceptingWithoutAnotherCreatureDoesNothing() {
        Permanent meep = addCreatureReady(player1, new TheMeep());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
