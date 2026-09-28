package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShellfishScholar.class, BogRats.class, GrizzlyBears.class, ThinkTwice.class})
@DisplayName("Shellfish Scholar")
class ShellfishScholarTest extends BaseCardTest {

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
