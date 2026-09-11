package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AnimusOfNightsReach;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLongReachOfNight.class, AnimusOfNightsReach.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheLongReachOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II let each opponent discard instead of sacrificing a creature")
    void opponentMayDiscardInsteadOfSacrificingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("The sacrifice option only offers creatures")
    void opponentCanSacrificeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId(),
                gd.playerBattlefields.get(player2.getId()).get(2).getId());

        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Chapter III transforms the Saga under its controller's control")
    void chapterIIITransformsIntoAnimus() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent animus = findPermanent(player1, "Animus of Night's Reach");
        assertThat(animus).isNotNull();
        assertThat(animus.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Animus gets +X/+0 for creature cards in the defending player's graveyard")
    void animusBoostsForDefendingPlayersCreatureCards() {
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new AnimusOfNightsReach());
        animus.setSummoningSick(false);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, animus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, animus)).isEqualTo(4);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLongReachOfNight());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
