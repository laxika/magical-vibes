package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchKingOfAngmar.class, GrizzlyBears.class})
class WitchKingOfAngmarTest extends BaseCardTest {

    @Test
    void discardingACardGrantsIndestructibleAndTapsWitchKing() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingOfAngmar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(witchKing.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, witchKing, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void combatDamageTriggerSacrificesOneDamagingCreatureAndTemptsTheRing() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingOfAngmar());
        Permanent attackerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent attackerTwo = addCreatureReady(player2, new GrizzlyBears());
        attackerOne.setAttacking(true);
        attackerTwo.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(attackerOne.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(attackerTwo);
        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), witchKing.getId());
        assertThat(gqs.hasEffectiveSupertype(gd, witchKing,
                com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    void controllerChoosesRingBearerWhenControllingMultipleCreatures() {
        Permanent witchKing = addCreatureReady(player1, new WitchKingOfAngmar());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, witchKing.getId());

        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), witchKing.getId());
        assertThat(gqs.hasEffectiveSupertype(gd, witchKing,
                com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, otherCreature,
                com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY)).isFalse();
    }
}
