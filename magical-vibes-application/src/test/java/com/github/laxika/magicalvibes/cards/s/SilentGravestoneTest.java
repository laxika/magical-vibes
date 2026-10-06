package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentGravestone.class, Disentomb.class, GrizzlyBears.class, Shock.class})
class SilentGravestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Cards in graveyards can't be targeted")
    void blocksGraveyardTargeting() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new SilentGravestone());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability exiles itself and all graveyards, then draws a card")
    void exilesSelfAndAllGraveyardsThenDraws() {
        Permanent gravestone = addReadyGravestone();
        Card libraryCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int gravestoneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gravestone);
        harness.activateAbility(player1, gravestoneIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gravestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Silent Gravestone"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));
    }

    private Permanent addReadyGravestone() {
        Permanent gravestone = harness.addToBattlefieldAndReturn(player1, new SilentGravestone());
        gravestone.setSummoningSick(false);
        return gravestone;
    }

    @Test
    void blocksOpponentsGraveyardTargeting() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, new SilentGravestone());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsWithEmptyGraveyardsAndExilesOnlyOnResolution() {
        Permanent gravestone = addReadyGravestone();
        Card drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gravestone.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gravestone);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gravestone);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void abilityStillExilesGraveyardsAndDrawsAfterSourceLeaves() {
        Permanent gravestone = addReadyGravestone();
        Card buried = new GrizzlyBears();
        Card drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player2, List.of(buried));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(gravestone);
        harness.setHand(player1, List.of(gravestone.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(buried);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(gravestone.getCard(), drawn);
    }

    @Test
    void cannotActivateWithoutFourManaOrWhileTapped() {
        Permanent gravestone = addReadyGravestone();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gravestone.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
