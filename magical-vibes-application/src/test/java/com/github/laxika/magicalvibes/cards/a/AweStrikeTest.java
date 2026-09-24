package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SpikeshotGoblin;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
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

@CardUsed({AweStrike.class, SpikeshotGoblin.class, YotianSoldier.class})
class AweStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the target creature's next noncombat damage and gains that much life")
    void preventsTargetCreaturesNextNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent spikeshotGoblin = addCreatureReady(player2, new SpikeshotGoblin());
        castAweStrike(spikeshotGoblin.getId());

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, indexOf(player2, spikeshotGoblin), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the target creature's next combat damage and gains the full prevented amount")
    void preventsTargetCreaturesNextCombatDamage() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, creatureWithPower(4));
        castAweStrike(attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the target creature's next damage to another creature and gains that much life")
    void preventsTargetCreaturesNextDamageToAnotherCreature() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new SpikeshotGoblin());
        Permanent recipient = addCreatureReady(player1, new YotianSoldier());
        castAweStrike(source.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, indexOf(player2, source), null, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A different creature's damage is not prevented and the shield remains")
    void doesNotPreventDamageFromDifferentCreature() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, creatureWithPower(4));
        Permanent other = addCreatureReady(player2, creatureWithPower(2));
        castAweStrike(target.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .anyMatch(shield -> shield.sourceId().equals(target.getId()));
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, creatureWithPower(4));
        castAweStrike(attacker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new AweStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAweStrike(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AweStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private YotianSoldier creatureWithPower(int power) {
        YotianSoldier card = new YotianSoldier();
        card.setPower(power);
        return card;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
