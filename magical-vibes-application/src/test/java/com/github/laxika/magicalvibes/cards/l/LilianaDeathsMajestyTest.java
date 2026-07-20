package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaDeathsMajestyTest extends BaseCardTest {

    // ===== +1: Create a 2/2 black Zombie token, mill two =====

    @Test
    @DisplayName("+1 creates a 2/2 black Zombie token, mills two, and raises loyalty")
    void plusOneCreatesZombieAndMills() {
        Permanent liliana = addReadyLiliana(player1, 5);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

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

    // ===== -3: Reanimate a creature from your graveyard as a black Zombie =====

    @Test
    @DisplayName("-3 returns a creature from your graveyard as a black Zombie and lowers loyalty")
    void minusThreeReanimatesAsBlackZombie() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Card bears = new GrizzlyBears();
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
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 cannot target a creature in an opponent's graveyard")
    void minusThreeCannotTargetOpponentGraveyard() {
        addReadyLiliana(player1, 5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== -7: Destroy all non-Zombie creatures =====

    @Test
    @DisplayName("-7 destroys non-Zombie creatures but spares Zombies")
    void minusSevenDestroysOnlyNonZombies() {
        addReadyLiliana(player1, 7);
        harness.addToBattlefield(player2, new GrizzlyBears()); // non-Zombie → destroyed
        harness.addToBattlefield(player2, new Gravedigger());  // Zombie → survives

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gravedigger"));
    }

    // ===== Helpers =====

    private Permanent addReadyLiliana(Player player, int loyalty) {
        LilianaDeathsMajesty card = new LilianaDeathsMajesty();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
