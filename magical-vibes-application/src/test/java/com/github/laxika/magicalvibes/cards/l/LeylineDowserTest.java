package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeylineDowser.class, Shock.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
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
        harness.addToBattlefield(player1, new LeylineDowser());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());

        findPermanent(player1, "Leyline Dowser").tap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Leyline Dowser").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
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
}
