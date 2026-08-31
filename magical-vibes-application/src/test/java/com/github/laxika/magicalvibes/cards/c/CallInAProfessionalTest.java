package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BoonOfSafety;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallInAProfessional.class, AirElemental.class, BoonOfSafety.class, FountainOfYouth.class})
class CallInAProfessionalTest extends BaseCardTest {

    @Test
    void dealsUnpreventableDamageToCreatureWithShieldCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addShieldCounter(creature);

        castCallInAProfessional(creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
    }

    @Test
    void dealsThreeDamageToTargetPlayerAndPreventsLifeGainThisTurn() {
        castCallInAProfessional(player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playersCantGainLifeThisTurn).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CallInAProfessional()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addShieldCounter(Permanent creature) {
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void castCallInAProfessional(UUID targetId) {
        harness.setHand(player1, List.of(new CallInAProfessional()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
