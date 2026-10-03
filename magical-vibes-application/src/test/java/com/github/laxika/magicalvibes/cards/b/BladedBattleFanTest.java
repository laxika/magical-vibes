package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladedBattleFan.class, BolaSlinger.class})
class BladedBattleFanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Bladed Battle-Fan attaches it and grants indestructible until end of turn")
    void enteringAttachesAndGrantsIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent fan = castBattleFan();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Bladed Battle-Fan's enter-the-battlefield indestructible grant expires at end of turn")
    void indestructibleExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        castBattleFan();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1} moves Bladed Battle-Fan and its bonus to another creature")
    void equipMovesFanAndBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent fan = harness.addToBattlefieldAndReturn(player1, new BladedBattleFan());
        fan.setAttachedTo(first.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moving the Equipment does not move the temporary indestructible grant")
    void equipDoesNotMoveIndestructible() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent fan = castBattleFan();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("The enter trigger grants indestructible even if the Equipment has left")
    void grantsIndestructibleWhenEquipmentLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent fan = castBattleFan();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fan);
        gd.playerGraveyards.get(player1.getId()).add(fan.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat with no creature to attach to")
    void flashWithNoControlledCreature() {
        harness.addToBattlefield(player2, new BolaSlinger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);

        Permanent fan = castBattleFan();

        assertThat(fan.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castBattleFan() {
        harness.setHand(player1, List.of(new BladedBattleFan()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Bladed Battle-Fan");
    }
}
