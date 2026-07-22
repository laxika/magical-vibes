package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlFromTheCellarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature from graveyard to hand and puts +1/+1 on Zombie")
    void returnsCreatureAndCountersZombie() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID zombieId = harness.getPermanentId(player1, "Diregraf Ghoul");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(zombieId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
        assertThat(findPermanent(player1, "Diregraf Ghoul")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can cast with only a graveyard target and no Zombie")
    void canCastWithOnlyGraveyardTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot put counter on non-Zombie you control")
    void cannotTargetNonZombie() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast with only a Zombie and no graveyard creature")
    void cannotCastWithoutGraveyardTarget() {
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID zombieId = harness.getPermanentId(player1, "Diregraf Ghoul");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(zombieId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Flashback returns creature and exiles the spell")
    void flashbackReturnsAndExiles() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new CrawlFromTheCellar(), creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Crawl from the Cellar"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }

    @Test
    @DisplayName("Flashback can also put a counter on a Zombie")
    void flashbackWithZombieTarget() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setGraveyard(player1, List.of(new CrawlFromTheCellar(), creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID zombieId = harness.getPermanentId(player1, "Diregraf Ghoul");
        harness.castFlashback(player1, 0, creature.getId(), List.of(zombieId));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Diregraf Ghoul")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }
}
