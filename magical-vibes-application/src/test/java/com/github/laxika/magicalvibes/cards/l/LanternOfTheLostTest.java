package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternOfTheLost.class, GrizzlyBears.class, Shock.class})
class LanternOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, exiles a target card from a graveyard")
    void entersAndExilesTargetGraveyardCard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new LanternOfTheLost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The activated ability exiles itself and all graveyards, then draws a card")
    void exilesSelfAndAllGraveyardsThenDraws() {
        Permanent lantern = new Permanent(new LanternOfTheLost());
        lantern.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lantern);
        Card libraryCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lanternIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lantern);
        harness.activateAbility(player1, lanternIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lantern);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(lantern.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));
    }

    @Test
    @DisplayName("The entry trigger can target a card in either player's graveyard")
    void entryTriggerTargetsOwnGraveyard() {
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new LanternOfTheLost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }
}
