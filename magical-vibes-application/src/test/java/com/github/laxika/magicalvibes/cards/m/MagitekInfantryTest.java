package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagitekInfantry.class, LeoninScimitar.class, GrizzlyBears.class})
class MagitekInfantryTest extends BaseCardTest {

    @Test
    void getsPlusOnePowerWithAnotherArtifact() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void doesNotCountItselfAsAnotherArtifact() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }

    @Test
    void searchesForMagitekInfantryAndPutsItOntoBattlefieldTapped() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new MagitekInfantry());
        infantry.setSummoningSick(false);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new MagitekInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Magitek Infantry"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Magitek Infantry"))
                .hasSize(2)
                .anyMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
