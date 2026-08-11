package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

class NightmareSowerTest extends BaseCardTest {

    private void setOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private UUID addTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        return harness.getPermanentId(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Putting a counter on a target creature is optional")
    void putsCounterOnChosenCreatureOrDeclines() {
        Permanent nightmareSower = harness.addToBattlefieldAndReturn(player1, new NightmareSower());
        UUID targetId = addTargetCreature();
        setOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(nightmareSower.getId(), targetId);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger can resolve without choosing a creature")
    void triggerCanChooseNoTarget() {
        harness.addToBattlefield(player1, new NightmareSower());
        UUID targetId = addTargetCreature();
        setOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger only offers creatures as targets")
    void onlyCreaturesAreTargets() {
        harness.addToBattlefield(player1, new NightmareSower());
        UUID creatureId = addTargetCreature();
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");
        setOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creatureId);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creatureId).doesNotContain(landId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The source creature remains a legal optional target when no other creature is present")
    void sourceRemainsALegalTarget() {
        Permanent nightmareSower = harness.addToBattlefieldAndReturn(player1, new NightmareSower());
        harness.addToBattlefield(player2, new Forest());
        setOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(nightmareSower.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting during your own turn does not trigger the ability")
    void ownTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new NightmareSower());
        UUID targetId = addTargetCreature();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
