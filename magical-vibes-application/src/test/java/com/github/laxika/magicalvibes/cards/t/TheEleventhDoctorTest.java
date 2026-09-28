package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEleventhDoctor.class, Forest.class, GrizzlyBears.class})
class TheEleventhDoctorTest extends BaseCardTest {

    @Test
    void combatDamageMayExileAnyCardFromHandWithManaValueCounters() {
        addCreatureReady(player1, new TheEleventhDoctor());
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(land, creature));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileCardFromHandWithTimeCountersChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThat(gd.exiledCardTimeCounters).containsEntry(land.getId(), 0);
    }

    @Test
    void activatedAbilityOnlyTargetsCreatureWithPowerThreeOrLess() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        largeCreature.setPowerModifier(2);
        largeCreature.setToughnessModifier(2);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityMakesAValidTargetUnblockableThisTurn() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }
}
