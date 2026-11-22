package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.b.BlueMagesCane;
import com.github.laxika.magicalvibes.cards.c.ClaimOfErebos;
import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.f.FlametongueKavu;
import com.github.laxika.magicalvibes.cards.f.FleetingReflection;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexParasite;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.cards.s.ShadesForm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StonewiseFortifier;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.cards.t.TimeEbb;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * What a player who may not look at a face-down permanent reads about it in the log, prompts, labels and errors
 * (CR 708.2, 708.5), and that they are told as it leaves the battlefield (CR 708.9).
 */
@CardUsed({BlueMagesCane.class, ClaimOfErebos.class, Cytoshape.class, ExaltedAngel.class, FlametongueKavu.class, FleetingReflection.class, GrizzlyBears.class,
        RealityRipple.class, ShadesForm.class, Shock.class, StonewiseFortifier.class, SwiftfootBoots.class, HexParasite.class,
        TimeEbb.class, VampireHexmage.class})
class FaceDownTextIdentityTest extends BaseCardTest {

    private static final String HIDDEN_NAME = "Exalted Angel";

    @Test
    @DisplayName("A trigger's chosen target doesn't name a face-down permanent")
    void triggerTargetIsNotNamed() {
        Permanent hidden = faceDownAngel();
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        prepareResponse(new FlametongueKavu(), ManaColor.RED);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, hidden.getId());

        assertHidden();
    }

    @Test
    @DisplayName("Phasing out doesn't name a face-down permanent, and neither does phasing in")
    void phasingDoesNotNamePermanent() {
        Permanent hidden = faceDownAngel();
        prepareResponse(new RealityRipple(), ManaColor.BLUE);
        harness.castInstant(player2, 0, hidden.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hidden);
        assertThat(logText()).anyMatch(line -> line.contains("phases out"));
        assertHidden();

        harness.forceActivePlayer(player1);
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hidden);
        assertHidden();
    }

    @Test
    @DisplayName("A copy log line doesn't name the face-down permanent that is copied")
    void copiedPermanentIsNotNamed() {
        Permanent hidden = faceDownAngel();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        prepareResponse(new FleetingReflection(), ManaColor.BLUE);
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player2, 0, List.of(bears.getId(), hidden.getId()));
        harness.passBothPriorities();

        assertThat(logText()).anyMatch(line -> line.contains("copy"));
        assertHidden();
    }

    @Test
    @DisplayName("A copy chosen as a spell resolves doesn't name the face-down permanent chosen")
    void chosenCopyIsNotNamed() {
        Permanent hidden = faceDownAngel();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        prepareResponse(new Cytoshape(), ManaColor.BLUE);
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, hidden.getId());

        assertThat(logText()).anyMatch(line -> line.contains("copies of"));
        assertHidden();
    }

    @Test
    @DisplayName("Removing all counters doesn't name a face-down permanent")
    void counterRemovalIsNotNamed() {
        Permanent hidden = faceDownAngel();
        hidden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new VampireHexmage());
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, hidden.getId());
        harness.passBothPriorities();

        assertThat(logText()).anyMatch(line -> line.contains("removes all counters"));
        assertHidden();
    }

    @Test
    @DisplayName("A prompt about another player's face-down permanent doesn't name it")
    void promptDoesNotNamePermanent() {
        Permanent hidden = faceDownAngel();
        hidden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new HexParasite());
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 1, hidden.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertHidden();
    }

    @Test
    @DisplayName("Prevention aimed at a face-down permanent doesn't name it")
    void preventionIsNotNamed() {
        Permanent hidden = faceDownAngel();
        addCreatureReady(player2, new StonewiseFortifier());
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, hidden.getId());
        harness.passBothPriorities();

        assertThat(logText()).anyMatch(line -> line.contains("prevented"));
        assertHidden();
    }

    @Test
    @DisplayName("The error for an illegal target doesn't name a face-down permanent")
    void errorDoesNotNamePermanent() {
        Permanent hidden = faceDownAngel();
        harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots()).setAttachedTo(hidden.getId());
        prepareResponse(new Shock(), ManaColor.RED);

        Throwable error = catchThrowable(() -> harness.castInstant(player2, 0, hidden.getId()));

        assertThat(error).isNotNull();
        assertThat(error.getMessage()).contains("hexproof").doesNotContain(HIDDEN_NAME);
    }

    @Test
    @DisplayName("Combat damage assignment doesn't name face-down blockers to the attacking player")
    void blockersAreNotNamed() {
        Permanent first = faceDownAngel();
        Permanent second = faceDownAngel();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        for (Permanent blocker : List.of(first, second)) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.addBlockingTargetId(attacker.getId());
        }
        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNotNull();
        assertHidden();
    }

    @Test
    @DisplayName("A face-down permanent put on top of a library is still revealed")
    void leavingForHiddenZoneIsRevealed() {
        Permanent hidden = faceDownAngel();
        prepareResponse(new TimeEbb(), ManaColor.BLUE);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, hidden.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(hidden.getCard().getId());
        assertThat(logText()).anyMatch(line -> line.contains(HIDDEN_NAME));
    }

    @Test
    @DisplayName("The log doesn't name a face-down permanent as the source of an ability it was granted")
    void grantedAbilitySourceIsNotNamed() {
        Permanent hidden = faceDownAngel();
        harness.addToBattlefieldAndReturn(player1, new ClaimOfErebos()).setAttachedTo(hidden.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(logText()).anyMatch(line -> line.contains("loses 2 life"));
        assertHidden();
    }

    @Test
    @DisplayName("A face-down permanent's boost of itself isn't labelled with its name")
    void boostSourceIsNotNamed() {
        Permanent hidden = faceDownAngel();
        harness.addToBattlefieldAndReturn(player1, new ShadesForm()).setAttachedTo(hidden.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hidden)).isEqualTo(3);
        assertHidden();
    }

    @Test
    @DisplayName("A triggered ability granted to a face-down permanent doesn't name it")
    void grantedTriggerIsNotNamed() {
        Permanent hidden = faceDownAngel();
        harness.addToBattlefieldAndReturn(player1, new BlueMagesCane()).setAttachedTo(hidden.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(logText()).anyMatch(line -> line.contains("trigger"));
        assertHidden();
    }

    private Permanent faceDownAngel() {
        Permanent permanent = addCreatureReady(player1, new ExaltedAngel());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();
        return permanent;
    }

    private void prepareResponse(Card card, ManaColor color) {
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, color, 6);
        harness.ensurePriority(player2);
    }

    private List<String> logText() {
        return gd.gameLog.stream().map(GameLogEntry::plainText).toList();
    }

    private void assertHidden() {
        harness.publishState();
        assertThat(logText()).noneMatch(line -> line.contains(HIDDEN_NAME));
        assertThat(harness.getConn2().getSentMessages()).isNotEmpty()
                .allSatisfy(message -> assertThat(message).doesNotContain(HIDDEN_NAME));
    }
}
