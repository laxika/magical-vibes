package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShellfishScholar.class, BogRats.class, GrizzlyBears.class, ThinkTwice.class, Conspiracy.class})
@DisplayName("Shellfish Scholar")
class ShellfishScholarTest extends BaseCardTest {

    @Test
    void ownEntryTriggersEvenWhenItIsNotARat() {
        harness.addToBattlefieldAndReturn(player1, new Conspiracy())
                .setChosenSubtype(CardSubtype.GOBLIN);

        harness.enterBattlefieldAndReturn(player1, new ShellfishScholar());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Think Twice");
    }

    @Test
    void opponentsRatDoesNotTrigger() {
        addCreatureReady(player1, new ShellfishScholar());

        harness.enterBattlefieldAndReturn(player2, new BogRats());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void reductionAppliesToMultipleSpellsAfterDroppingBelowThreshold() {
        addCreatureReady(player1, new ShellfishScholar());
        harness.setGraveyard(player1, List.of(
                new ThinkTwice(), new ThinkTwice(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromGraveyard(player1, 0);
        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionDoesNotApplyToSpellsFromHand() {
        addCreatureReady(player1, new ShellfishScholar());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionDoesNotApplyToOpponentsGraveyardSpells() {
        addCreatureReady(player1, new ShellfishScholar());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Conjures Think Twice when it enters")
    void conjuresThinkTwiceWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new ShellfishScholar());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .singleElement()
                .satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Think Twice");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    @DisplayName("Conjures Think Twice when another Rat enters")
    void conjuresThinkTwiceWhenAnotherRatEnters() {
        addCreatureReady(player1, new ShellfishScholar());

        harness.enterBattlefieldAndReturn(player1, new BogRats());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Think Twice");
    }

    @Test
    @DisplayName("A non-Rat entering does not trigger the conjure ability")
    void nonRatDoesNotTrigger() {
        addCreatureReady(player1, new ShellfishScholar());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Threshold ability reduces a graveyard spell by two generic mana")
    void thresholdAbilityReducesGraveyardSpellCost() {
        addCreatureReady(player1, new ShellfishScholar());
        harness.setGraveyard(player1, List.of(
                new ThinkTwice(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Threshold ability cannot activate before seven cards")
    void thresholdAbilityDoesNotActivateBeforeSevenCards() {
        addCreatureReady(player1, new ShellfishScholar());
        harness.setGraveyard(player1, List.of(
                new ThinkTwice(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
