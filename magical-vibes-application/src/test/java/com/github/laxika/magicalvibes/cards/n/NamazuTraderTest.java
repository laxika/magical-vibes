package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NamazuTrader.class, Forest.class, GrizzlyBears.class, DarksteelRelic.class})
class NamazuTraderTest extends BaseCardTest {

    @Test
    void entersWithLifeLossAndTreasure() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new NamazuTrader()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void maySacrificeAnotherCreatureOrArtifactToSurveilTwo() {
        addCreatureReady(player1, new NamazuTrader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card top0 = new Forest();
        Card top1 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top0, top1));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
        assertThat(surveil.toGraveyard()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
    }

    @Test
    void maySacrificeAnotherArtifactToSurveilTwo() {
        addCreatureReady(player1, new NamazuTrader());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Card top0 = new Forest();
        Card top1 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top0, top1));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, relic.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(relic);
    }
}
