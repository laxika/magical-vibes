package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SiegeZombie;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrawlFromTheCellar.class, SiegeZombie.class, CatharCommando.class, ThinkTwice.class})
class CrawlFromTheCellarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature from graveyard to hand and puts +1/+1 on Zombie")
    void returnsCreatureAndCountersZombie() {
        Card graveyardCreature = new CatharCommando();
        harness.addToBattlefield(player1, new SiegeZombie());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID zombieId = harness.getPermanentId(player1, "Siege Zombie");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(zombieId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
        assertThat(findPermanent(player1, "Siege Zombie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can cast with only a graveyard target and no Zombie")
    void canCastWithOnlyGraveyardTarget() {
        Card creature = new CatharCommando();
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
        harness.assertInGraveyard(player1, "Crawl from the Cellar");
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        Card instant = new ThinkTwice();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot put counter on non-Zombie you control")
    void cannotTargetNonZombie() {
        Card creature = new CatharCommando();
        harness.addToBattlefield(player1, new CatharCommando());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID nonZombieId = harness.getPermanentId(player1, "Cathar Commando");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of(nonZombieId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast with only a Zombie and no graveyard creature")
    void cannotCastWithoutGraveyardTarget() {
        harness.addToBattlefield(player1, new SiegeZombie());
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID zombieId = harness.getPermanentId(player1, "Siege Zombie");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(zombieId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Flashback returns creature and exiles the spell")
    void flashbackReturnsAndExiles() {
        Card creature = new CatharCommando();
        harness.setGraveyard(player1, List.of(new CrawlFromTheCellar(), creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Crawl from the Cellar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }

    @Test
    @DisplayName("Flashback can also put a counter on a Zombie")
    void flashbackWithZombieTarget() {
        Card creature = new CatharCommando();
        harness.addToBattlefield(player1, new SiegeZombie());
        harness.setGraveyard(player1, List.of(new CrawlFromTheCellar(), creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID zombieId = harness.getPermanentId(player1, "Siege Zombie");
        harness.castFlashback(player1, 0, creature.getId(), List.of(zombieId));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Siege Zombie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new CatharCommando();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsZombie() {
        Card creature = new CatharCommando();
        UUID zombieId = harness.addToBattlefieldAndReturn(player2, new SiegeZombie()).getId();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of(zombieId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDeclineZombieTargetEvenWhenOneIsAvailable() {
        Card creature = new CatharCommando();
        harness.addToBattlefield(player1, new SiegeZombie());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cathar Commando");
        assertThat(findPermanent(player1, "Siege Zombie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillCountersZombieWhenGraveyardTargetLeaves() {
        Card creature = new CatharCommando();
        UUID zombieId = harness.addToBattlefieldAndReturn(player1, new SiegeZombie()).getId();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, creature.getId(), List.of(zombieId));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Cathar Commando");
        assertThat(findPermanent(player1, "Siege Zombie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Crawl from the Cellar");
    }

    @Test
    void stillReturnsCreatureWhenZombieChangesController() {
        Card creature = new CatharCommando();
        var zombie = harness.addToBattlefieldAndReturn(player1, new SiegeZombie());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CrawlFromTheCellar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, creature.getId(), List.of(zombie.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(zombie);
        gd.playerBattlefields.get(player2.getId()).add(zombie);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cathar Commando");
        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Crawl from the Cellar");
    }

    @Test
    void flashbackIsExiledWhenItsOnlyTargetLeaves() {
        Card creature = new CatharCommando();
        harness.setGraveyard(player1, List.of(new CrawlFromTheCellar(), creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castFlashback(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Cathar Commando");
        harness.assertNotInGraveyard(player1, "Crawl from the Cellar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crawl from the Cellar"));
    }
}
