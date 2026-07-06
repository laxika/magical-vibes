package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheParish;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZombieApocalypseTest extends BaseCardTest {

    private void castZombieApocalypse() {
        harness.setHand(player1, List.of(new ZombieApocalypse()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    

    @Test
    @DisplayName("Returns all Zombie creature cards from your graveyard to the battlefield tapped")
    void returnsAllZombieCreaturesFromGraveyardTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card gravecrawler = new Gravecrawler();
        Card zombieGoliath = new ZombieGoliath();
        harness.setGraveyard(player1, List.of(gravecrawler, zombieGoliath));

        castZombieApocalypse();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).anyMatch(p -> p.getCard().getName().equals("Gravecrawler") && p.isTapped());
        assertThat(battlefield).anyMatch(p -> p.getCard().getName().equals("Zombie Goliath") && p.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gravecrawler"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Zombie Goliath"));
    }

    @Test
    @DisplayName("Does not return non-Zombie creatures from your graveyard")
    void doesNotReturnNonZombieCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card bears = new GrizzlyBears();
        Card gravecrawler = new Gravecrawler();
        harness.setGraveyard(player1, List.of(bears, gravecrawler));

        castZombieApocalypse();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not return Zombie creatures from opponent's graveyard")
    void doesNotReturnOpponentZombies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentZombie = new Gravecrawler();
        harness.setGraveyard(player2, List.of(opponentZombie));

        castZombieApocalypse();

        harness.assertNotOnBattlefield(player1, "Gravecrawler");
        harness.assertNotOnBattlefield(player2, "Gravecrawler");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Gravecrawler"));
    }

    @Test
    @DisplayName("Destroys all Humans on the battlefield after returning Zombies")
    void destroysAllHumansAfterReturningZombies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new ChampionOfTheParish());
        harness.addToBattlefield(player2, new ChampionOfTheParish());
        harness.setGraveyard(player1, List.of(new Gravecrawler()));

        castZombieApocalypse();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        harness.assertNotOnBattlefield(player1, "Champion of the Parish");
        harness.assertNotOnBattlefield(player2, "Champion of the Parish");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Champion of the Parish"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Champion of the Parish"));
    }

    @Test
    @DisplayName("Returned Zombies survive the Human destruction step")
    void returnedZombiesSurviveHumanDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new ChampionOfTheParish());
        harness.setGraveyard(player1, List.of(new Gravecrawler(), new ZombieGoliath()));

        castZombieApocalypse();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gravecrawler"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie Goliath"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Champion of the Parish"));
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castZombieApocalypse();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Zombie Apocalypse"));
    }
}
