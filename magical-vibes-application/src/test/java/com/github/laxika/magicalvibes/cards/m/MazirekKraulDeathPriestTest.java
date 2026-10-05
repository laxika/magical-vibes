package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazirekKraulDeathPriest.class, GrizzlyBears.class, KuldothaRebirth.class, Spellbook.class,
        CruelEdict.class, AllIsDust.class, Ornithopter.class, TurnToFrog.class})
class MazirekKraulDeathPriestTest extends BaseCardTest {

    @Test
    @DisplayName("A sacrificed permanent puts a +1/+1 counter on each controlled creature")
    void sacrificedPermanentPutsCountersOnControlledCreatures() {
        Permanent mazirek = addCreatureReady(player1, new MazirekKraulDeathPriest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        castKuldothaRebirth(player1, artifact);
        resolveAllTriggers();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's sacrificed creature also triggers Mazirek")
    void opponentsSacrificedCreatureTriggers() {
        Permanent mazirek = addCreatureReady(player1, new MazirekKraulDeathPriest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        resolveAllTriggers();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice-cost triggers resolve before the spell creates creatures")
    void sacrificeCostTriggerDoesNotCounterNewTokens() {
        Permanent mazirek = addCreatureReady(player1, new MazirekKraulDeathPriest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        castKuldothaRebirth(player1, artifact);
        resolveAllTriggers();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin")).hasSize(3)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Mazirek triggers for each other permanent sacrificed simultaneously with it")
    void simultaneousSacrificeStillCountersSurvivingCreatures() {
        harness.addToBattlefield(player1, new MazirekKraulDeathPriest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Mazirek alone does not trigger its ability")
    void sacrificingOnlyMazirekDoesNotPutCounters() {
        harness.addToBattlefield(player2, new MazirekKraulDeathPriest());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mazirek cannot trigger while it has lost all abilities")
    void losingAbilitiesPreventsSacrificeTrigger() {
        Permanent mazirek = harness.addToBattlefieldAndReturn(player1, new MazirekKraulDeathPriest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, mazirek.getId());

        castKuldothaRebirth(player1, artifact);
        resolveAllTriggers();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castKuldothaRebirth(Player player, Permanent artifact) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new KuldothaRebirth()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, artifact.getId());
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
    }
}
