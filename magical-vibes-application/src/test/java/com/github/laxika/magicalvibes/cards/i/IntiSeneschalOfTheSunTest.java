package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.b.BitterTriumph;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({IntiSeneschalOfTheSun.class, GrizzlyBears.class, Shock.class, Forest.class, BitterTriumph.class})
class IntiSeneschalOfTheSunTest extends BaseCardTest {

    @Test
    void discardingOnAttackTargetsAnAttackingCreatureForCounterAndTrample() {
        addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Forest discarded = new Forest();
        Shock exiled = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(exiled));

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBeforeCast = gd.getLife(player2.getId());
        harness.castFromExile(player1, exiled.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCast - 2);
    }

    @Test
    void decliningTheAttackAbilityDoesNotDiscardOrExile() {
        addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Shock discarded = new Shock();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void exiledCardCannotBeCastOnceTheNextEndStepBegins() {
        Permanent inti = addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Shock exiled = new Shock();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(exiled));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, inti.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);

        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    @Test
    void intiNeedNotAttackAndNonattackingCreaturesAreNotLegalTargets() {
        Permanent inti = addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, inti.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingWithAnEmptyHandDoesNotCreateAReflexiveTriggerOrExile() {
        Permanent inti = addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(inti.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gqs.hasKeyword(gd, inti, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void discardingAsASpellCostExilesBeforeTheSpellResolves() {
        Permanent inti = addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Forest discarded = new Forest();
        Forest exiled = new Forest();
        harness.setHand(player1, List.of(new BitterTriumph(), discarded));
        harness.setLibrary(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithDiscard(player1, 0, inti.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        harness.assertOnBattlefield(player1, "Inti, Seneschal of the Sun");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(inti.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Inti, Seneschal of the Sun");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }
}
