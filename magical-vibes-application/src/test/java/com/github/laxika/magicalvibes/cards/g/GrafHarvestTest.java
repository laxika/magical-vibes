package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Graf Harvest")
class GrafHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Gives Zombies you control menace")
    void givesOwnZombiesMenace() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Gravecrawler());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Gravecrawler"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Gravecrawler"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Exiles a creature card and creates an untapped Zombie")
    void exilesCreatureAndCreatesZombie() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
