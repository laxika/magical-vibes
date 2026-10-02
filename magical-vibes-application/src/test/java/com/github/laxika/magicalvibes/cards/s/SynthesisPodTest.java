package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynthesisPod.class, Divination.class, Forest.class, GrizzlyBears.class})
class SynthesisPodTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any spell and finds a card with one higher mana value")
    void exilesAnySpellAndFindsCardWithOneHigherManaValue() {
        GrizzlyBears sourceSpell = activateWithCreatureSpell();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(sourceSpell.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Divination");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Forest");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
    }

    @Test
    @DisplayName("Leaves the matching card exiled when the free cast is declined")
    void leavesMatchingCardExiledWhenDeclined() {
        activateWithCreatureSpell();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Divination");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Divination"));
    }

    private GrizzlyBears activateWithCreatureSpell() {
        GrizzlyBears sourceSpell = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SynthesisPod(), sourceSpell));
        harness.setLibrary(player2, List.of(new Forest(), new Divination(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sourceSpell.getId()));
        return sourceSpell;
    }
}
