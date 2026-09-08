package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.s.Scarecrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EaterOfTheDead.class, Scarecrow.class, CityOfShadows.class})
class EaterOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("When tapped, Eater of the Dead exiles a creature card and untaps")
    void tappedEaterExilesCreatureAndUntaps() {
        Permanent eater = addReadyEater();
        eater.tap();
        Card creature = new Scarecrow();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The ability can be activated while Eater of the Dead is untapped")
    void untappedEaterCanActivateButDoesNothing() {
        Permanent eater = addReadyEater();
        Card creature = new Scarecrow();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void rejectsNonCreatureTarget() {
        Permanent eater = addReadyEater();
        Card land = new CityOfShadows();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(eater), 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedEaterExilesCreatureFromItsControllersGraveyard() {
        Permanent eater = addReadyEater();
        eater.tap();
        Card creature = new Scarecrow();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
    }

    @Test
    void untappingBeforeResolutionPreventsExileAndUntapEffect() {
        Permanent eater = addReadyEater();
        eater.tap();
        Card creature = new Scarecrow();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        eater.untap();
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionDoesNotUntapSource() {
        Permanent eater = addReadyEater();
        eater.tap();
        Card creature = new Scarecrow();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    void summoningSickEaterCanActivateAbility() {
        Permanent eater = harness.addToBattlefieldAndReturn(player1, new EaterOfTheDead());
        eater.tap();
        Card creature = new Scarecrow();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    private Permanent addReadyEater() {
        Permanent eater = harness.addToBattlefieldAndReturn(player1, new EaterOfTheDead());
        eater.setSummoningSick(false);
        return eater;
    }

    private int indexOf(Permanent eater) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(eater);
    }
}
