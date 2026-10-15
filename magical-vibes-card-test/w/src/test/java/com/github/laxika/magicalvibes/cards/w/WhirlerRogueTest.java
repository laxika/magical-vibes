package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GuardianAutomaton;
import com.github.laxika.magicalvibes.cards.m.Meteorite;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlerRogue.class, GuardianAutomaton.class, TimberpackWolf.class, Meteorite.class})
class WhirlerRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 1/1 flying Thopter artifact creature tokens")
    void createsTwoThopters() {
        harness.setHand(player1, List.of(new WhirlerRogue()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        for (Permanent thopter : thopters) {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(thopter.getCard().getColor()).isNull();
        }
    }

    @Test
    @DisplayName("Tapping two artifacts makes the target creature unblockable this turn")
    void abilityMakesTargetUnblockable() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        Permanent artifact1 = addCreatureReady(player1, new GuardianAutomaton());
        Permanent artifact2 = addCreatureReady(player1, new GuardianAutomaton());
        Permanent artifact3 = addCreatureReady(player1, new GuardianAutomaton());
        Permanent attacker = addCreatureReady(player1, new TimberpackWolf());

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);
        harness.activateAbility(player1, rogueIdx, null, attacker.getId());
        harness.handlePermanentChosen(player1, artifact1.getId());
        harness.handlePermanentChosen(player1, artifact2.getId());
        harness.passBothPriorities();

        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(artifact3.isTapped()).isFalse();

        Permanent blocker = addCreatureReady(player2, new TimberpackWolf());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Newly created Thopters can pay the cost while the Rogue is summoning sick")
    void newlyCreatedThoptersCanPayCost() {
        harness.setHand(player1, List.of(new WhirlerRogue()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent rogue = findPermanent(player1, "Whirler Rogue");
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rogue),
                null, rogue.getId());
        assertThat(thopters).allMatch(Permanent::isTapped);
        assertThat(rogue.isTapped()).isFalse();
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new TimberpackWolf());
        rogue.setSummoningSick(false);
        rogue.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature artifacts can pay the cost and an opponent's creature can be targeted")
    void opponentCreatureAndNoncreatureArtifacts() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new WhirlerRogue());
        Permanent artifact1 = harness.addToBattlefieldAndReturn(player1, new Meteorite());
        Permanent artifact2 = harness.addToBattlefieldAndReturn(player1, new Meteorite());
        Permanent target = addCreatureReady(player2, new TimberpackWolf());
        Permanent blocker = addCreatureReady(player1, new TimberpackWolf());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rogue),
                null, target.getId());
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers(player2);
        int blockerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature can be blocked again after turn cleanup")
    void unblockabilityExpiresAtEndOfTurn() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        harness.addToBattlefield(player1, new Meteorite());
        harness.addToBattlefield(player1, new Meteorite());
        Permanent target = addCreatureReady(player1, new TimberpackWolf());
        Permanent blocker = addCreatureReady(player2, new TimberpackWolf());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rogue),
                null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(target.isCantBeBlocked()).isFalse();

        target.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Tapped artifacts and opponents' artifacts cannot pay the cost")
    void cannotUseTappedOrOpposingArtifacts() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        addCreatureReady(player1, new GuardianAutomaton());
        Permanent tapped = addCreatureReady(player1, new GuardianAutomaton());
        tapped.tap();
        addCreatureReady(player2, new GuardianAutomaton());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rogue), null, rogue.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability with only one untapped artifact")
    void cannotActivateWithOneArtifact() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        addCreatureReady(player1, new GuardianAutomaton());
        Permanent target = addCreatureReady(player1, new TimberpackWolf());

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);

        assertThatThrownBy(() -> harness.activateAbility(player1, rogueIdx, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        addCreatureReady(player1, new GuardianAutomaton());
        addCreatureReady(player1, new GuardianAutomaton());

        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Meteorite());

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);

        assertThatThrownBy(() -> harness.activateAbility(player1, rogueIdx, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
