package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NulltreadGargantuanTest extends BaseCardTest {

    private void castNulltreadGargantuan() {
        harness.setHand(player1, List.of(new NulltreadGargantuan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("With no other creatures it must put itself on top of its owner's library")
    void topsItselfWithNoOtherCreatures() {
        castNulltreadGargantuan();
        harness.passBothPriorities(); // resolve ETB -> forced self-top

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nulltread Gargantuan"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Nulltread Gargantuan"));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo("Nulltread Gargantuan");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With another creature the controller is prompted to choose")
    void promptsWhenAnotherCreaturePresent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castNulltreadGargantuan();
        harness.passBothPriorities(); // resolve ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nulltread Gargantuan"));
    }

    @Test
    @DisplayName("Choosing another creature tops it and keeps Nulltread Gargantuan")
    void choosingAnotherCreatureTopsIt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castNulltreadGargantuan();
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nulltread Gargantuan"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Controller may choose Nulltread Gargantuan itself even with another creature")
    void mayChooseItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castNulltreadGargantuan();
        harness.passBothPriorities();

        UUID nulltreadId = harness.getPermanentId(player1, "Nulltread Gargantuan");
        harness.handlePermanentChosen(player1, nulltreadId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nulltread Gargantuan"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Nulltread Gargantuan");
    }
}
