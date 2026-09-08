package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimFlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Grim Flayer +2/+2")
    void deliriumBoostsGrimFlayer() {
        Permanent flayer = addCreatureReady(player1, new GrimFlayer());

        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));

        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage to a player triggers surveil 3")
    void combatDamageTriggersSurveilThree() {
        addCreatureReady(player1, new GrimFlayer());
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top0, top1, top2));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1, top2);
        assertThat(surveil.toGraveyard()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1, top2);
    }
}
