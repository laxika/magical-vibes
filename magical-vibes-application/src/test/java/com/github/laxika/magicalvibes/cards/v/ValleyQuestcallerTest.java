package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BeckCall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntrepidRabbit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyQuestcaller.class, BeckCall.class, GrizzlyBears.class, IntrepidRabbit.class})
class ValleyQuestcallerTest extends BaseCardTest {

    @Test
    void buffsOtherControlledRabbitsAndNotOtherCreaturesOrOpponents() {
        Permanent rabbit = addCreatureReady(player1, new IntrepidRabbit());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRabbit = addCreatureReady(player2, new IntrepidRabbit());

        int rabbitPower = gqs.getEffectivePower(gd, rabbit);
        int rabbitToughness = gqs.getEffectiveToughness(gd, rabbit);
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opponentRabbitPower = gqs.getEffectivePower(gd, opponentRabbit);
        int opponentRabbitToughness = gqs.getEffectiveToughness(gd, opponentRabbit);
        addCreatureReady(player1, new ValleyQuestcaller());

        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(rabbitPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, rabbit)).isEqualTo(rabbitToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.getEffectivePower(gd, opponentRabbit)).isEqualTo(opponentRabbitPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentRabbit)).isEqualTo(opponentRabbitToughness);
    }

    @Test
    void scriesOnceWhenSeveralMatchingCreaturesEnterTogether() {
        Permanent questcaller = addCreatureReady(player1, new ValleyQuestcaller());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack)
                .filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getId().equals(questcaller.getCard().getId()))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(4)
                .allSatisfy(bird -> {
                    assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
                });

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
