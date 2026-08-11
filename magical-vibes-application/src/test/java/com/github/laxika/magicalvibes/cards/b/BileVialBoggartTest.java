package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BileVialBoggartTest extends BaseCardTest {

    private void setupCombatWhereBoggartDies() {
        Permanent boggart = findPermanent(player1, "Bile-Vial Boggart");
        boggart.setSummoningSick(false);
        boggart.setAttacking(true);

        GrizzlyBears blocker = new GrizzlyBears();
        blocker.setPower(3);
        blocker.setToughness(3);
        Permanent blockerPermanent = new Permanent(blocker);
        blockerPermanent.setSummoningSick(false);
        blockerPermanent.setBlocking(true);
        blockerPermanent.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPermanent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Bile-Vial Boggart dies, it can put a -1/-1 counter on a creature")
    void deathTriggerPutsCounterOnChosenCreature() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereBoggartDies();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger may choose no target")
    void deathTriggerMayChooseNoTarget() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereBoggartDies();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger only offers creatures as targets")
    void deathTriggerOnlyOffersCreatures() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Forest");

        setupCombatWhereBoggartDies();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creatureId).doesNotContain(landId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The death trigger still resolves with no creature to target")
    void deathTriggerResolvesWithoutLegalCreature() {
        harness.addToBattlefield(player1, new BileVialBoggart());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
