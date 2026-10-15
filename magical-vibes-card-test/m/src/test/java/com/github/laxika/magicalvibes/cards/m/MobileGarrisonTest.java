package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.ImplementOfCombustion;
import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobileGarrison.class, AetherChaser.class, ImplementOfCombustion.class})
class MobileGarrisonTest extends BaseCardTest {

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastTwo() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent creature = addCreatureReady(player1, new AetherChaser());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, garrison)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void attackTriggerTargetsAnotherArtifactOrCreatureIControl() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent crew = addCreatureReady(player1, new AetherChaser());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfCombustion());
        Permanent opponentCreature = addCreatureReady(player2, new AetherChaser());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownArtifact.getId(), crew.getId())
                .doesNotContain(garrison.getId(), opponentCreature.getId());
    }

    @Test
    void attackTriggerUntapsTheChosenPermanent() {
        addReadyGarrison(player1);
        addCreatureReady(player1, new AetherChaser());
        Permanent target = addCreatureReady(player1, new AetherChaser());
        target.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void attackTriggerUntapsANoncreatureArtifact() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent crew = addCreatureReady(player1, new AetherChaser());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfCombustion());
        artifact.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(crew.isTapped()).isTrue();
        assertThat(garrison.isTapped()).isTrue();
    }

    @Test
    void attackTriggerDoesNotUntapAPermanentAnOpponentNowControls() {
        addReadyGarrison(player1);
        addCreatureReady(player1, new AetherChaser());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfCombustion());
        artifact.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void attackTriggerResolvesAfterGarrisonLeavesTheBattlefield() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent crew = addCreatureReady(player1, new AetherChaser());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crew.getId());
        gd.playerBattlefields.get(player1.getId()).remove(garrison);
        gd.playerGraveyards.get(player1.getId()).add(garrison.getCard());
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isFalse();
    }

    private Permanent addReadyGarrison(Player player) {
        return addCreatureReady(player, new MobileGarrison());
    }
}
