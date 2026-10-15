package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianaDeathsMajesty.class, Colossapede.class, Gravedigger.class, HazeOfPollen.class})
class LilianaDeathsMajestyTest extends BaseCardTest {


    @Test
    @DisplayName("+1 creates a 2/2 black Zombie token, mills two, and raises loyalty")
    void plusOneCreatesZombieAndMills() {
        Permanent liliana = addReadyLiliana(player1, 5);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede(), new Colossapede()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);

        // Two cards milled from library into the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }


    @Test
    @DisplayName("-3 returns a creature from your graveyard as a black Zombie and lowers loyalty")
    void minusThreeReanimatesAsBlackZombie() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Card bears = new Colossapede();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 1, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(returned.getGrantedColors()).contains(CardColor.BLACK);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("-3 cannot target a non-creature card in your graveyard")
    void minusThreeCannotTargetNonCreature() {
        addReadyLiliana(player1, 5);
        Card instant = new HazeOfPollen();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 cannot target a creature in an opponent's graveyard")
    void minusThreeCannotTargetOpponentGraveyard() {
        addReadyLiliana(player1, 5);
        Card bears = new Colossapede();
        harness.setGraveyard(player2, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("-7 destroys non-Zombie creatures but spares Zombies")
    void minusSevenDestroysOnlyNonZombies() {
        addReadyLiliana(player1, 7);
        harness.addToBattlefield(player2, new Colossapede()); // non-Zombie → destroyed
        harness.addToBattlefield(player2, new Gravedigger());  // Zombie → survives

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Colossapede");
        harness.assertOnBattlefield(player2, "Gravedigger");
    }


    @Test
    @DisplayName("+1 still creates a Zombie when fewer than two cards remain in the library")
    void plusOneWithShortLibrary() {
        addReadyLiliana(player1, 5);
        Card lastCard = new Colossapede();
        harness.setLibrary(player1, List.of(lastCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("-3 does not return a target that leaves the graveyard before resolution")
    void minusThreeWithRemovedTarget() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Card creature = new Colossapede();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Colossapede");
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("A reanimated creature keeps its colors and types and survives -7 after Liliana dies")
    void reanimatedZombieSurvivesUltimate() {
        addReadyLiliana(player1, 3);
        Card creature = new Colossapede();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liliana, Death's Majesty");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.INSECT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.ZOMBIE)).isTrue();
        assertThat(returned.isTapped()).isFalse();

        addReadyLiliana(player1, 7);
        harness.addToBattlefield(player1, new Colossapede());
        harness.addToBattlefield(player2, new Colossapede());
        harness.addToBattlefield(player2, new Gravedigger());
        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(returned);
        harness.assertNotOnBattlefield(player2, "Colossapede");
        harness.assertOnBattlefield(player2, "Gravedigger");
        harness.assertInGraveyard(player1, "Colossapede");
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaDeathsMajesty());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
