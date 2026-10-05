package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.t.TheWarGames;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JamieMcCrimmon.class, Millstone.class, GrizzlyBears.class, HangarbackWalker.class,
        TheWarGames.class})
class JamieMcCrimmonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic artifact boosts Jamie by its mana value")
    void historicSpellBoostsByManaValue() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a nonhistoric spell does not boost Jamie")
    void nonHistoricSpellDoesNotTrigger() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jamie loses the boost at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(4);

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    @Test
    void chosenXCountsForEachManaSymbolInHistoricSpell() {
        Permanent jamie = addJamie();
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(8);
    }

    @Test
    void legendaryCreatureSpellTriggersBeforeItResolves() {
        Permanent jamie = addJamie();
        harness.setHand(player1, List.of(new JamieMcCrimmon()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonlegendarySagaSpellIsHistoric() {
        Permanent jamie = addJamie();
        harness.setHand(player1, List.of(new TheWarGames()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(6);
    }

    @Test
    void opponentsHistoricSpellDoesNotTrigger() {
        Permanent jamie = addJamie();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Millstone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    @Test
    void multipleHistoricSpellsAccumulateBoosts() {
        Permanent jamie = addJamie();
        harness.setHand(player1, List.of(new Millstone(), new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(6);
    }

    @Test
    void puttingHistoricPermanentOntoBattlefieldDoesNotTrigger() {
        Permanent jamie = addJamie();

        harness.enterBattlefieldAndReturn(player1, new Millstone());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    @Test
    void historicBoostAllowsTramplingOverBlocker() {
        addCreatureReady(player1, new JamieMcCrimmon());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Jamie McCrimmon");
    }

    private Permanent addJamie() {
        return harness.addToBattlefieldAndReturn(player1, new JamieMcCrimmon());
    }
}
