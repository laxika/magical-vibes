package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinThePainArtist.class, ScavengingOoze.class})
class NinThePainArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Nin deals X damage and the target creature's controller draws X cards")
    void dealsDamageAndTargetControllerDraws() {
        addReadyNin(player1);
        Permanent target = addCreatureReady(player2, new ScavengingOoze());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ScavengingOoze(), new ScavengingOoze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("X=0 deals no damage and draws no cards")
    void zeroXDoesNothing() {
        addReadyNin(player1);
        Permanent target = addCreatureReady(player2, new ScavengingOoze());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ScavengingOoze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Nin can target only a creature")
    void cannotTargetPlayer() {
        addReadyNin(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nin can target herself and draws cards before dying to lethal damage")
    void canTargetHerself() {
        addReadyNin(player1);
        Permanent nin = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NinThePainArtist(), new NinThePainArtist()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, nin.getId());
        assertThat(nin.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nin.getCard());
    }

    @Test
    @DisplayName("Preventing all damage does not reduce the number of cards drawn")
    void drawsChosenXEvenWhenDamageIsPrevented() {
        addReadyNin(player1);
        Permanent target = addCreatureReady(player2, new NinThePainArtist());
        target.setDamagePreventionShield(3);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new NinThePainArtist(), new NinThePainArtist(), new NinThePainArtist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Removing the only target before resolution prevents drawing")
    void missingTargetDoesNotDraw() {
        addReadyNin(player1);
        Permanent target = addCreatureReady(player2, new NinThePainArtist());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new NinThePainArtist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nin's ability still deals damage and draws cards after Nin leaves the battlefield")
    void abilityResolvesWithoutSource() {
        addReadyNin(player1);
        Permanent nin = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent target = addCreatureReady(player2, new NinThePainArtist());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new NinThePainArtist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(nin);
        gd.playerGraveyards.get(player1.getId()).add(nin.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The target's controller at resolution draws, even if control changed after activation")
    void currentControllerDrawsAfterControlChanges() {
        addReadyNin(player1);
        Permanent target = addCreatureReady(player2, new ScavengingOoze());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new NinThePainArtist()));
        harness.setLibrary(player2, List.of(new NinThePainArtist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nin cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        addReadyNin(player1);
        Permanent nin = gd.playerBattlefields.get(player1.getId()).getFirst();
        nin.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, nin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyNin(Player player) {
        addCreatureReady(player, new NinThePainArtist());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
