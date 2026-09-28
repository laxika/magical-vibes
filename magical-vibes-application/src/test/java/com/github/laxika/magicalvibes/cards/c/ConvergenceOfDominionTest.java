package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConvergenceOfDominion.class, GrizzlyBears.class, ReassemblingSkeleton.class})
class ConvergenceOfDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Commander control reduces graveyard activated ability costs by {2}")
    void commanderControlReducesGraveyardAbilityCost() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.addToBattlefield(player1, commander);
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Graveyard abilities are not reduced without a controlled commander")
    void noCommanderMeansNoReduction() {
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The tap ability mills three cards")
    void tapAbilityMillsThreeCards() {
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
