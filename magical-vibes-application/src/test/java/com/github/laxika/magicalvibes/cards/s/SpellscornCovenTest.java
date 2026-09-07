package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.TakeItBack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellscornCoven.class, TakeItBack.class, GrizzlyBears.class, Opt.class})
class SpellscornCovenTest extends BaseCardTest {

    @Test
    void entersAndMakesEachOpponentDiscardACard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        SpellscornCoven card = new SpellscornCoven();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void adventureReturnsTargetSpellToItsOwnersHandAndExilesThisCard() {
        Opt targetSpell = new Opt();
        SpellscornCoven card = new SpellscornCoven();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(card));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAdventure(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Opt");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
