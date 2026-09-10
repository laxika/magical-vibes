package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bullwhip;
import com.github.laxika.magicalvibes.cards.c.Conviction;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfEssence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NomadsEnKor.class, Bullwhip.class, Conviction.class, HonorGuard.class, Shock.class,
        WallOfEssence.class})
class NomadsEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent destination = addCreatureReady(player1, new WallOfEssence());
        Permanent attacker = addCreatureReady(player2, new HonorGuard());

        harness.activateAbility(player1, indexOf(player1, nomads), null, destination.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, nomads), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(nomads.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only one damage from a two-damage event is redirected")
    void redirectsOnlyOneDamageFromLargerEvent() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent destination = addCreatureReady(player1, new WallOfEssence());

        harness.setHand(player1, List.of(new Conviction(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, nomads.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, nomads), null, destination.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, nomads.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(nomads.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nomads);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new WallOfEssence());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, nomads), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, nomads), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a controlled noncreature permanent")
    void cannotTargetControlledNoncreature() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, nomads), null, bullwhip.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent destination = addCreatureReady(player1, new WallOfEssence());

        harness.activateAbility(player1, indexOf(player1, nomads), null, destination.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
