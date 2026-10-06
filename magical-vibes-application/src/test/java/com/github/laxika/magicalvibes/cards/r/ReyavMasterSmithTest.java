package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReyavMasterSmith.class, GrizzlyBears.class, HolyStrength.class, Bonesplitter.class})
class ReyavMasterSmithTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted and equipped attackers gain double strike")
    void enchantedAndEquippedAttackersGainDoubleStrike() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent plain = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, plain, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The granted double strike expires at end of turn")
    void doubleStrikeExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Reyav can grant double strike to itself when equipped")
    void equippedReyavGainsDoubleStrike() {
        Permanent reyav = addCreatureReady(player1, new ReyavMasterSmith());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(reyav.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, reyav, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Being both enchanted and equipped produces only one trigger")
    void enchantedAndEquippedCreatureTriggersOnce() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(attacker.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An Aura controlled by an opponent qualifies the attacker")
    void opponentControlledAuraQualifiesAttacker() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(attacker.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opposing enchanted attackers do not trigger Reyav")
    void opposingAttackerDoesNotGainDoubleStrike() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Losing Equipment before resolution does not stop the grant")
    void losingEquipmentBeforeResolutionStillGrantsDoubleStrike() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(gd.stack).hasSize(1);
        equipment.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Becoming equipped after attacking does not create a trigger")
    void becomingEquippedAfterAttackingDoesNotTrigger() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(gd.stack).isEmpty();
        equipment.setAttachedTo(attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
