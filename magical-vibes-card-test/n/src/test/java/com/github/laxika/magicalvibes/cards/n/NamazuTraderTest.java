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

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(relic);
    }

    @Test
    void decliningSacrificeLeavesLibraryAndCreatureUntouched() {
        addCreatureReady(player1, new NamazuTrader());
        Permanent other = addCreatureReady(player1, new NamazuTrader());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeChoiceExcludesSourceLandsAndOpponentsCreatures() {
        addCreatureReady(player1, new NamazuTrader());
        Permanent other = addCreatureReady(player1, new NamazuTrader());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new NamazuTrader());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(other.getId());
    }

    @Test
    void surveilCanKeepBothCardsInChosenOrderDuringAttackAbilityResolution() {
        addCreatureReady(player1, new NamazuTrader());
        Permanent other = addCreatureReady(player1, new NamazuTrader());
        Card first = new Forest();
        Card second = new NamazuTrader();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.stack).isEmpty();
    }
}
