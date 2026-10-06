package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimFlayer.class, GrizzlyBears.class, Plains.class, Shock.class, Millstone.class})
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

    @Test
    void deliriumCountsDistinctTypesOnlyInControllersGraveyardAndUpdatesContinuously() {
        Permanent flayer = addCreatureReady(player1, new GrimFlayer());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Plains(), new Shock()));
        harness.setGraveyard(player2, List.of(new Millstone()));

        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(2);
    }

    @Test
    void surveilCanSplitCardsAndReorderTheRemainder() {
        Permanent flayer = addCreatureReady(player1, new GrimFlayer());
        Card top0 = new Millstone();
        Card top1 = new Plains();
        Card top2 = new Shock();
        Card untouched = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));
        harness.setLibrary(player1, List.of(top0, top1, top2, untouched));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top2, top1, untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0).doesNotContain(top1, top2);
        assertThat(gqs.getEffectivePower(gd, flayer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flayer)).isEqualTo(4);
    }

    @Test
    void surveilWithTwoCardsCanKeepAllInChosenOrder() {
        addCreatureReady(player1, new GrimFlayer());
        Card top0 = new Plains();
        Card top1 = new Shock();
        harness.setLibrary(player1, List.of(top0, top1));
        harness.setGraveyard(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top1, top0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilWithEmptyLibraryFinishesWithoutPromptOrDrawing() {
        addCreatureReady(player1, new GrimFlayer());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageOnlyToBlockerDoesNotTriggerSurveil() {
        addCreatureReady(player1, new GrimFlayer());
        addCreatureReady(player2, new GrizzlyBears());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player1, List.of());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
