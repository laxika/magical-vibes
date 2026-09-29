package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterFormedAnew.class, GrizzlyBears.class})
class TheMasterFormedAnewTest extends BaseCardTest {

    @Test
    void bodyThiefExilesAndMarksAControlledCreature() {
        Card creature = new GrizzlyBears();
        addCreatureReady(player1, creature);
        harness.setHand(player1, List.of(new TheMasterFormedAnew()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.exiledCardsWithTakeoverCounters).contains(creature.getId());
    }

    @Test
    void entersAsACopyOfATakeoverMarkedCreatureInExile() {
        Card exiledCreature = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledCreature));
        gd.exiledCardsWithTakeoverCounters.add(exiledCreature.getId());
        harness.setHand(player1, List.of(new TheMasterFormedAnew()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(exiledCreature.getName()));
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
