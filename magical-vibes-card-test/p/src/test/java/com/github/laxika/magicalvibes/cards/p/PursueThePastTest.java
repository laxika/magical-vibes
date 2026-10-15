package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PursueThePast.class})
class PursueThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gains 2 life without choosing to discard")
    void gainsLifeWithoutDiscarding() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new PursueThePast(), new PursueThePast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Choosing to discard draws two cards")
    void discardDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new PursueThePast(), new PursueThePast()));
        harness.setHand(player1, List.of(new PursueThePast(), new PursueThePast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An empty hand gains life but cannot draw without discarding")
    void emptyHandDoesNotDraw() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setLibrary(player1, List.of(new PursueThePast(), new PursueThePast()));
        harness.setHand(player1, List.of(new PursueThePast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback gains life, discards and draws, then exiles the spell")
    void flashbackResolvesAndExiles() {
        PursueThePast spell = new PursueThePast();
        PursueThePast discarded = new PursueThePast();
        PursueThePast firstDraw = new PursueThePast();
        PursueThePast secondDraw = new PursueThePast();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(spell));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard during flashback still gains life and exiles the spell")
    void flashbackWithoutDiscarding() {
        PursueThePast spell = new PursueThePast();
        PursueThePast retained = new PursueThePast();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(retained));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(spell));
    }
}
