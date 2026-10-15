package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonGirlAndDevilDinosaur.class, GrizzlyBears.class, Ornithopter.class, Panharmonicon.class})
class MoonGirlAndDevilDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an artifact you control enters, only once each turn")
    void artifactEntryDrawsOnlyOnceEachTurn() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castArtifactFromHand();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);

        castArtifactFromHand();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Drawing the second card sets base power and toughness to 6/6 and grants trample")
    void secondDrawBoostsSourceUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();

        draw(player1);
        resolveTopOfStack();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();
    }

    private void castArtifactFromHand() {
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void artifactDrawCanBeTheSecondDrawAndBoostTheSource() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        resolveTopOfStack();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void opponentsArtifactDoesNotTriggerOrConsumeTheTurnLimit() {
        addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void controllerSecondDrawTriggersOnOpponentsTurnButOpponentsDrawsDoNot() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void artifactTriggerLimitAppliesBeforeTheFirstTriggerResolves() {
        addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({MoonGirlAndDevilDinosaur.class, Panharmonicon.class, Ornithopter.class, GrizzlyBears.class})
    void panharmoniconCannotMakeTheOncePerTurnArtifactAbilityTriggerTwice() {
        addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void artifactAbilityCanTriggerAgainDuringTheNextPlayersTurn() {
        addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        resolveTopOfStack();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
