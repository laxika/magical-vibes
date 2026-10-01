package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZephyrScribe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurlyBadgersaur.class, ZephyrScribe.class, GrizzlyBears.class, Shock.class, Forest.class})
class SurlyBadgersaurTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card puts a +1/+1 counter on Surly Badgersaur")
    void creatureDiscardPutsCounter() {
        Permanent badgersaur = addBadgersaur();
        Permanent scribe = prepareDiscard(new GrizzlyBears(), new Forest());

        discardOneCard(scribe);
        resolveAllTriggers();

        assertThat(badgersaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding a land card creates a Treasure")
    void landDiscardCreatesTreasure() {
        addBadgersaur();
        Permanent scribe = prepareDiscard(new Forest(), new Shock());

        discardOneCard(scribe);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Discarding a noncreature, nonland card fights up to one opposing creature")
    void noncreatureNonlandDiscardFightsOpponentCreature() {
        Permanent badgersaur = addBadgersaur();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent scribe = prepareDiscard(new Shock(), new Forest());

        discardOneCard(scribe);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(badgersaur.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The fight trigger cannot target a creature you control")
    void fightTriggerCannotTargetOwnCreature() {
        addBadgersaur();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent scribe = prepareDiscard(new Shock(), new Forest());

        discardOneCard(scribe);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBadgersaur() {
        return harness.addToBattlefieldAndReturn(player1, new SurlyBadgersaur());
    }

    private Permanent prepareDiscard(Card discardedCard, Card libraryCard) {
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new ZephyrScribe());
        scribe.setSummoningSick(false);
        harness.setHand(player1, List.of(discardedCard));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        return scribe;
    }

    private void discardOneCard(Permanent scribe) {
        int scribeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scribe);
        harness.activateAbility(player1, scribeIndex, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
