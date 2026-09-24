package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodbraidMarauder.class, GrizzlyBears.class, LlanowarElves.class, Millstone.class,
        Mountain.class, Shock.class})
class BloodbraidMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Bloodbraid Marauder cascade when cast")
    void deliriumGivesCascade() {
        LlanowarElves llanowarElves = new LlanowarElves();
        harness.setGraveyard(player1, fourCardTypes());
        harness.setLibrary(player1, List.of(new Mountain(), llanowarElves));
        castMarauder();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(llanowarElves);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof LlanowarElves
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Without delirium, Bloodbraid Marauder does not cascade")
    void doesNotCascadeWithoutDelirium() {
        LlanowarElves llanowarElves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Mountain(), new Shock()));
        harness.setLibrary(player1, List.of(llanowarElves));
        castMarauder();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(llanowarElves);
    }

    @Test
    @DisplayName("Bloodbraid Marauder cannot block")
    void cannotBlock() {
        addCreatureReady(player1, new BloodbraidMarauder());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMarauder() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BloodbraidMarauder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private List<Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Mountain(), new Shock(), new Millstone());
    }
}
