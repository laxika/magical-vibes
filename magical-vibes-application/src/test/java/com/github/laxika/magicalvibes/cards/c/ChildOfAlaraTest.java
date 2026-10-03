package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BeaconBehemoth;
import com.github.laxika.magicalvibes.cards.i.InkwellLeviathan;
import com.github.laxika.magicalvibes.cards.a.ArmillarySphere;
import com.github.laxika.magicalvibes.cards.m.MarkOfAsylum;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildOfAlara.class, BeaconBehemoth.class, InkwellLeviathan.class, Forest.class, Plains.class,
        ArmillarySphere.class, MarkOfAsylum.class})
class ChildOfAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("When Child of Alara dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();
        assignChildDamageToBlocker(blocker);

        harness.assertInGraveyard(player1, "Child of Alara");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Child of Alara");
    }

    @Test
    @DisplayName("Resolving the death trigger destroys all nonland permanents but spares lands")
    void destroysAllNonlandPermanents() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        harness.addToBattlefield(player1, new BeaconBehemoth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();
        assignChildDamageToBlocker(blocker);
        harness.passBothPriorities();

        // Every nonland permanent is gone on both battlefields; only lands remain.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(p -> p.getCard().hasType(CardType.LAND))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(p -> p.getCard().hasType(CardType.LAND))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));

        // The other creatures were destroyed and put into their owners' graveyards.
        harness.assertInGraveyard(player1, "Beacon Behemoth");
        harness.assertInGraveyard(player2, "Inkwell Leviathan");
    }

    @Test
    @DisplayName("Death trigger destroys artifacts and enchantments without targeting them")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        harness.addToBattlefield(player1, new ArmillarySphere());
        harness.addToBattlefield(player2, new MarkOfAsylum());
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();
        assignChildDamageToBlocker(blocker);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Armillary Sphere");
        harness.assertInGraveyard(player2, "Mark of Asylum");
        harness.assertInGraveyard(player2, "Inkwell Leviathan");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Death trigger ignores an existing regeneration shield")
    void cannotRegenerateOtherPermanents() {
        harness.addToBattlefield(player1, new ChildOfAlara());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BeaconBehemoth());
        creature.setRegenerationShield(1);
        Permanent blocker = setupCombatWhereChildDies();

        harness.passBothPriorities();
        assignChildDamageToBlocker(blocker);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Beacon Behemoth");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    /**
     * Child of Alara (6/6 trample) attacks and is blocked by a 7/11 that deals it lethal damage while
     * surviving combat itself, so the blocker remains when the death trigger resolves.
     * Returns the blocker so the caller can supply Child's trample damage assignment.
     */
    private Permanent setupCombatWhereChildDies() {
        Permanent childPerm = findPermanent(player1, "Child of Alara");
        childPerm.setSummoningSick(false);
        childPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new InkwellLeviathan());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return blockerPerm;
    }

    /**
     * Child (trample) has only 6 power against an 11-toughness blocker, so all 6 must be assigned to the
     * blocker (nothing tramples over). Resolving the assignment deals combat damage and kills Child.
     */
    private void assignChildDamageToBlocker(Permanent blocker) {
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
    }
}
