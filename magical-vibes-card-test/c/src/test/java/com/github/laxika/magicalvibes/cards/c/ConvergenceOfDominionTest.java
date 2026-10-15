package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.t.TrazynTheInfinite;
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

@CardUsed({ConvergenceOfDominion.class, TrazynTheInfinite.class, ReassemblingSkeleton.class,
        CanoptekTombSentinel.class})
class ConvergenceOfDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Commander control reduces graveyard activated ability costs by {2}")
    void commanderControlReducesGraveyardAbilityCost() {
        Card commander = new TrazynTheInfinite();
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
        Card first = new ConvergenceOfDominion();
        Card second = new ConvergenceOfDominion();
        Card third = new ConvergenceOfDominion();
        Card remaining = new ConvergenceOfDominion();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Multiple Convergences cannot reduce a generic-only graveyard ability to zero mana")
    void genericOnlyAbilityStillRequiresOneMana() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ConvergenceOfDominion());
        }
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Convergences allow a generic-only graveyard ability for one mana")
    void genericOnlyAbilityCanBeActivatedForOneMana() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ConvergenceOfDominion());
        }
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Controlling another player's commander does not enable the reduction")
    void opponentsCommanderDoesNotEnableReduction() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cost reduction does not remove colored mana requirements")
    void coloredManaRequirementRemains() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mill three mills all remaining cards when the library has fewer than three")
    void shortLibraryMillsRemainingCards() {
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        Card remaining = new ConvergenceOfDominion();
        harness.setLibrary(player1, List.of(remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Your commander controlled by an opponent does not enable the reduction")
    void commanderControlledByOpponentDoesNotEnableReduction() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Commander control does not reduce the battlefield mill activation cost")
    void battlefieldAbilityStillCostsThreeMana() {
        Card commander = new TrazynTheInfinite();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, new ConvergenceOfDominion());
        harness.addToBattlefield(player1, commander);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The mill ability pays three mana and taps its source before resolving")
    void millAbilityPaysManaAndTapsBeforeResolution() {
        var source = harness.addToBattlefieldAndReturn(player1, new ConvergenceOfDominion());
        Card milled = new ConvergenceOfDominion();
        Card opponentCard = new ConvergenceOfDominion();
        harness.setLibrary(player1, List.of(milled));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(milled);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
