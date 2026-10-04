package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrafHarvest.class, Gravecrawler.class, GrizzlyBears.class, Shock.class})
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

    @Test
    @DisplayName("Exile is paid before resolution and the ability survives source removal")
    void costIsPaidBeforeResolutionAndAbilitySurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Zombie");
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each activation exiles one creature and creates one Zombie")
    void canActivateRepeatedlyWithoutTapping() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleGraveyardCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Zombie"))
                .hasSize(2);
    }

    @Test
    @DisplayName("An opponent's creature graveyard cannot pay the activation cost")
    void cannotExileOpponentsCreature() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("The activation requires black mana")
    void cannotActivateWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new GrafHarvest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
