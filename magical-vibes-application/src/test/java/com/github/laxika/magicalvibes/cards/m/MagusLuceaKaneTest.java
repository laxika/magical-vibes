package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevilsPlay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnollspineInvocation;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MagusLuceaKane.class, GrizzlyBears.class, DevilsPlay.class, KnollspineInvocation.class})
class MagusLuceaKaneTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature")
    void putsCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new MagusLuceaKane());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies the next spell with X in its mana cost")
    void copiesNextXSpell() {
        harness.addToBattlefield(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Copies the next activated ability with X in its activation cost")
    void copiesNextXActivatedAbility() {
        harness.addToBattlefield(player1, new MagusLuceaKane());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
