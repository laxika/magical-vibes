package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcumDagssonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        Permanent arcum = harness.addToBattlefieldAndReturn(player1, new ArcumDagsson());
        arcum.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent sacrifices the targeted artifact creature and may search their own library")
    void opponentSacrificesAndSearchesOwnLibrary() {
        Permanent arcum = harness.addToBattlefieldAndReturn(player1, new ArcumDagsson());
        arcum.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        setLibrary(player2, new GrizzlyBears(), new Spellbook(), new GoldMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gold Myr");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.ARTIFACT))
                .noneMatch(card -> card.hasType(CardType.CREATURE));

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Declining the search leaves the sacrificed creature in the graveyard")
    void decliningSearchDoesNotSearch() {
        Permanent arcum = harness.addToBattlefieldAndReturn(player1, new ArcumDagsson());
        arcum.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        setLibrary(player2, new Spellbook());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(List.of(cards));
    }
}
