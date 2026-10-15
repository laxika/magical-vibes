package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.TakeItBack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellscornCoven.class, TakeItBack.class, GrizzlyBears.class, Opt.class})
class SpellscornCovenTest extends BaseCardTest {

    @Test
    void entersAndMakesEachOpponentDiscardACard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
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

    @Test
    void opponentChoosesOneCardAndControllerKeepsTheirHand() {
        SpellscornCoven keptByController = new SpellscornCoven();
        SpellscornCoven keptByOpponent = new SpellscornCoven();
        SpellscornCoven discarded = new SpellscornCoven();
        harness.setHand(player1, List.of(keptByController));
        harness.setHand(player2, List.of(keptByOpponent, discarded));

        harness.enterBattlefieldAndReturn(player1, new SpellscornCoven());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptByController);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    void emptyOpponentHandDoesNotPreventEntering() {
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new SpellscornCoven());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellscorn Coven");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void adventureReturnsCreatureSpellAndCreatureCanThenBeCastFromExile() {
        SpellscornCoven target = new SpellscornCoven();
        SpellscornCoven adventurer = new SpellscornCoven();
        harness.setHand(player1, List.of(target, adventurer));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(adventurer.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Spellscorn Coven");

        harness.castFromExile(player1, adventurer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellscorn Coven");
        assertThat(gd.findExiledCard(adventurer.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
    }
}
