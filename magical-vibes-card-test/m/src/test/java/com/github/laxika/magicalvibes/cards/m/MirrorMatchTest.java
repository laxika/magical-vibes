package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorMatch.class, AirElemental.class, GrizzlyBears.class, GloriousAnthem.class,
        GiantGrowth.class, FaerieConclave.class, JaceBeleren.class, PhantomWarrior.class, SoulWarden.class})
class MirrorMatchTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a copy blocking each creature attacking the caster")
    void createsBlockingCopiesForEachAttacker() {
        Permanent airElemental = addCreatureReady(player1, new AirElemental());
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        assertThat(airElemental.isAttacking()).isTrue();
        assertThat(airElemental.getAttackTarget()).isEqualTo(player2.getId());
        castMirrorMatch();

        List<Permanent> copies = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).anyMatch(copy -> copy.getCard().getName().equals("Air Elemental")
                && copy.isBlocking() && copy.getBlockingTargetIds().contains(airElemental.getId()));
        assertThat(copies).anyMatch(copy -> copy.getCard().getName().equals("Grizzly Bears")
                && copy.isBlocking() && copy.getBlockingTargetIds().contains(grizzlyBears.getId()));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .allMatch(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);
    }

    @Test
    @DisplayName("Copies are exiled at end of combat")
    void copiesAreExiledAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        harness.addToBattlefield(player2, new GloriousAnthem());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.getAttackTarget()).isEqualTo(player2.getId());
        castMirrorMatch();

        Permanent copy = findPermanent(player2, "Air Elemental");
        assertThat(copy.getBlockingTargetIds()).containsExactly(attacker.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(copy);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot be cast outside the declare blockers step")
    void cannotBeCastOutsideDeclareBlockers() {
        addCreatureReady(player1, new AirElemental());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MirrorMatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void copiesCreaturesAttackingYourPlaneswalker() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        declareAttackersAndPrepareBlockers(List.of(0));
        attacker.setAttackTarget(planeswalker.getId());

        castMirrorMatch();

        Permanent copy = findPermanent(player2, "Air Elemental");
        assertThat(copy.isBlocking()).isTrue();
        assertThat(copy.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    void copiesCanBlockUnblockableAttackers() {
        Permanent attacker = addCreatureReady(player1, new PhantomWarrior());
        declareAttackersAndPrepareBlockers(List.of(0));

        castMirrorMatch();

        Permanent copy = findPermanent(player2, "Phantom Warrior");
        assertThat(copy.isBlocking()).isTrue();
        assertThat(copy.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    void copiesDoNotCopyTemporaryPowerChangesOrTappedStatus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);

        castMirrorMatch();

        Permanent copy = findPermanent(player2, "Grizzly Bears");
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
    }

    @Test
    void animatedLandCopiesEnterWithoutBlocking() {
        Permanent attacker = addCreatureReady(player1, new FaerieConclave());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        castMirrorMatch();

        Permanent copy = findPermanent(player2, "Faerie Conclave");
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(copy.isBlocking()).isFalse();
        assertThat(copy.getBlockingTargetIds()).isEmpty();
    }

    @Test
    void endOfCombatExileUsesARespondableDelayedTrigger() {
        addCreatureReady(player1, new AirElemental());
        harness.addToBattlefield(player2, new GloriousAnthem());
        declareAttackersAndPrepareBlockers(List.of(0));
        castMirrorMatch();
        Permanent copy = findPermanent(player2, "Air Elemental");

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(copy);
    }

    @Test
    void copiesEnterSimultaneouslyAndSeeEachOtherEnter() {
        addCreatureReady(player1, new SoulWarden());
        addCreatureReady(player1, new SoulWarden());
        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        castMirrorMatch();
        resolveAllTriggers();

        harness.assertLife(player2, 22);
    }

    @Test
    void doesNotCopyCreaturesThatAreNotAttacking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AirElemental());
        harness.forceActivePlayer(player1);

        castMirrorMatch();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void castMirrorMatch() {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MirrorMatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
    }
}
