package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexplateWallbreaker.class, GrizzlyBears.class})
class HexplateWallbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Hexplate Wallbreaker creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new HexplateWallbreaker()));
        harness.addMana(player1, RED, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wallbreaker = findPermanent(player1, "Hexplate Wallbreaker");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(wallbreaker.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(4);
    }

    @Test
    @DisplayName("An equipped creature attacking in the first combat untaps all attackers and adds a combat")
    void firstCombatAttackUntapsAllAttackersAndAddsCombat() {
        Permanent attachedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wallbreaker = addWallbreakerReady(player1);
        wallbreaker.setAttachedTo(attachedCreature.getId());

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(attachedCreature.isTapped()).isTrue();
        assertThat(otherAttacker.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(attachedCreature.isTapped()).isFalse();
        assertThat(otherAttacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not untap attackers or add another combat")
    void laterCombatAttackDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wallbreaker = addWallbreakerReady(player1);
        wallbreaker.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 2);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private Permanent addWallbreakerReady(Player player) {
        Permanent permanent = new Permanent(new HexplateWallbreaker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
