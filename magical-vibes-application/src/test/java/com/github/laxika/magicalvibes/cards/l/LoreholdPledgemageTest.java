package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoreholdPledgemage.class, BarkshellBlessing.class, ExpandedAnatomy.class, GiantGrowth.class, GrizzlyBears.class})
class LoreholdPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Lorehold Pledgemage until end of turn")
    void castingInstantBoostsLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(3);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant boosts Lorehold Pledgemage")
    void copyingInstantBoostsLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(4);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Lorehold Pledgemage")
    void castingCreatureDoesNotBoostLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a sorcery triggers a boost before that sorcery resolves")
    void castingSorceryBoostsBeforeSpellResolves() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player2, new LoreholdPledgemage());
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(3);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's sorcery does not trigger magecraft")
    void opponentSorceryDoesNotBoostLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent opponentPledgemage = addCreatureReady(player2, new LoreholdPledgemage());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ExpandedAnatomy()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player2, 0, opponentPledgemage.getId());
        resolveAllTriggers();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentPledgemage.getEffectivePower()).isEqualTo(5);
        assertThat(opponentPledgemage.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Each spell cast adds a separate magecraft boost")
    void multipleCastsAccumulateBoosts() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player2, new LoreholdPledgemage());
        harness.setHand(player1, List.of(new ExpandedAnatomy(), new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();
        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(4);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage back")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pledgemage);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
