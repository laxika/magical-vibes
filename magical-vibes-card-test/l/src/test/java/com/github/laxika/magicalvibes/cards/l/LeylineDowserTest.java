package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeylineDowser.class, Shock.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, Ponder.class})
class LeylineDowserTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}: mills one card")
    void millsOneCard() {
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("returns a milled instant or sorcery to hand when accepted")
    void returnsMilledSpellToHand() {
        Card shock = new Shock();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("does not return a milled non-instant or non-sorcery card")
    void doesNotReturnMilledNonSpell() {
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("tapping a legendary creature untaps Leyline Dowser")
    void tappingLegendaryCreatureUntapsDowser() {
        var dowser = harness.addToBattlefieldAndReturn(player1, new LeylineDowser());
        var legend = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        dowser.tap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(dowser.isTapped()).isFalse();
        assertThat(legend.isTapped()).isTrue();
    }

    @Test
    @DisplayName("a nonlegendary creature cannot pay the untap cost")
    void nonlegendaryCreatureCannotPayUntapCost() {
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Leyline Dowser").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyTheSorceryMilledThisWay() {
        Card oldSpell = new Ponder();
        Card milledSpell = new Ponder();
        Card nextSpell = new Ponder();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setGraveyard(player1, List.of(oldSpell));
        harness.setLibrary(player1, List.of(milledSpell, nextSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledSpell).doesNotContain(oldSpell, nextSpell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextSpell);
        assertThat(findPermanent(player1, "Leyline Dowser").isTapped()).isTrue();
    }

    @Test
    void mayLeaveMilledSpellInGraveyard() {
        Card spell = new Ponder();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void emptyLibraryDoesNotReturnAnExistingSpell() {
        Card spell = new Ponder();
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLegendaryCreatureCannotPayUntapCost() {
        var dowser = harness.addToBattlefieldAndReturn(player1, new LeylineDowser());
        var legend = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        dowser.tap();
        legend.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dowser.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsLegendaryCreatureCannotPayUntapCost() {
        var dowser = harness.addToBattlefieldAndReturn(player1, new LeylineDowser());
        var legend = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        dowser.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dowser.isTapped()).isTrue();
        assertThat(legend.isTapped()).isFalse();
    }

    @Test
    void untapCostIsPaidBeforeTheAbilityResolves() {
        var dowser = harness.addToBattlefieldAndReturn(player1, new LeylineDowser());
        var legend = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        dowser.tap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(legend.isTapped()).isTrue();
        assertThat(dowser.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(dowser.isTapped()).isFalse();
        assertThat(legend.isTapped()).isTrue();
    }
}
