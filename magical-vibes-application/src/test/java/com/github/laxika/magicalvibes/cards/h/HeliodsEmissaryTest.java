package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeliodsEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("When Heliod's Emissary attacks, it taps a target creature an opponent controls")
    void creatureAttackTapsOpponentCreature() {
        addReadyEmissary(player1);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and its attack trigger still taps an opponent creature")
    void bestowBoostsAndGrantsAttackTrigger() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger only allows creatures controlled by an opponent")
    void attackTriggerRestrictsTargets() {
        addReadyEmissary(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    private Permanent addReadyEmissary(com.github.laxika.magicalvibes.model.Player player) {
        Permanent emissary = new Permanent(new HeliodsEmissary());
        emissary.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(emissary);
        return emissary;
    }
}
