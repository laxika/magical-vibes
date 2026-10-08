package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlyRequisitioner.class, Spellbook.class, GrizzlyBears.class, Memnite.class, LiquimetalCoating.class})
class SlyRequisitionerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Servo when a nontoken artifact you control is put into a graveyard")
    void createsServoForOwnNontokenArtifact() {
        harness.addToBattlefield(player1, new SlyRequisitioner());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        removeToGraveyard(artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for tokens, opponent artifacts, nonartifacts, or artifacts returned to hand")
    void ignoresNonMatchingPermanents() {
        harness.addToBattlefield(player1, new SlyRequisitioner());

        Card tokenCard = new Spellbook();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bouncedArtifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        removeToGraveyard(token);
        removeToGraveyard(opponentArtifact);
        removeToGraveyard(nonartifact);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bouncedArtifact));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }

    @Test
    void improvisePaysGenericManaWithSummoningSickArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new SlyRequisitioner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sly Requisitioner");
    }

    @Test
    void triggerResolvesAfterRequisitionerLeavesBattlefield() {
        Permanent requisitioner = harness.addToBattlefieldAndReturn(player1, new SlyRequisitioner());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        assertThat(gd.stack).hasSize(1);
        removeToGraveyard(requisitioner);

        harness.assertOnBattlefield(player1, "Servo");
        harness.assertInGraveyard(player1, "Sly Requisitioner");
    }

    @Test
    void triggersForCreatureMadeArtifactBeforeDying() {
        harness.addToBattlefield(player1, new SlyRequisitioner());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        removeToGraveyard(creature);

        harness.assertOnBattlefield(player1, "Servo");
    }

    @Test
    void triggersForOwnDeathWhenMadeArtifact() {
        Permanent requisitioner = harness.addToBattlefieldAndReturn(player1, new SlyRequisitioner());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.activateAbility(player1, 1, null, requisitioner.getId());
        harness.passBothPriorities();

        removeToGraveyard(requisitioner);

        harness.assertOnBattlefield(player1, "Servo");
        harness.assertInGraveyard(player1, "Sly Requisitioner");
    }

    @Test
    void eachArtifactCreatesServoButServoDeathDoesNotReplaceIt() {
        harness.addToBattlefield(player1, new SlyRequisitioner());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        removeToGraveyard(firstArtifact);
        removeToGraveyard(secondArtifact);

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(servos).hasSize(2);
        removeToGraveyard(servos.getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }
    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
