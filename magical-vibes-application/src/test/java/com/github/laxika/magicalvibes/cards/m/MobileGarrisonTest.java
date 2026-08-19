package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MobileGarrisonTest extends BaseCardTest {

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastTwo() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, garrison)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void attackTriggerTargetsAnotherArtifactOrCreatureIControl() {
        Permanent garrison = addReadyGarrison(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

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
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyGarrison(Player player) {
        Permanent garrison = new Permanent(new MobileGarrison());
        garrison.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(garrison);
        return garrison;
    }
}
