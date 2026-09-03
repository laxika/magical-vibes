package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrypticGateway.class, GrizzlyBears.class, LlanowarElves.class, MothdustChangeling.class})
class CrypticGatewayTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures that need not share a type and only offers a creature sharing with both")
    void tapsTwoCreaturesAndFiltersHandByBothTypes() {
        Permanent gateway = harness.addToBattlefieldAndReturn(player1, new CrypticGateway());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card invalidCreature = new GrizzlyBears();
        Card validCreature = new MothdustChangeling();
        harness.setHand(player1, List.of(invalidCreature, validCreature));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gateway.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(elves.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(((PendingInteraction.HandChoice) gameData.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gameData.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(validCreature.getId()))).isTrue();
        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(invalidCreature);
    }

    @Test
    @DisplayName("Declining the may choice leaves the tapped creatures tapped and puts nothing onto the battlefield")
    void decliningMayDoesNotPutCreatureOntoBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new CrypticGateway());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card creature = new MothdustChangeling();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gameData = harness.getGameData();
        assertThat(bears.isTapped()).isTrue();
        assertThat(elves.isTapped()).isTrue();
        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gameData.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()))).isFalse();
    }
}
