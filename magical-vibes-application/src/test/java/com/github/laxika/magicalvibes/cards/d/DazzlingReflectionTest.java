package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DazzlingReflection.class, GrizzlyBears.class, ProdigalPyromancer.class})
class DazzlingReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the target creature's power immediately")
    void gainsLifeEqualToTargetCreaturePower() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castDazzlingReflection(target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Prevents the target creature's next noncombat damage")
    void preventsTargetCreaturesNextNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        castDazzlingReflection(pyromancer.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the target creature's next combat damage")
    void preventsTargetCreaturesNextCombatDamage() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, creatureWithPower(4));
        castDazzlingReflection(attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A different creature's damage is not prevented and the shield remains")
    void doesNotPreventDamageFromDifferentCreature() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, creatureWithPower(4));
        Permanent other = addCreatureReady(player2, creatureWithPower(2));
        castDazzlingReflection(target.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .anyMatch(shield -> shield.sourceId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DazzlingReflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the creature's power at resolution rather than casting")
    void usesPowerAtResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DazzlingReflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(3);

        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("An absent target gives no life and creates no prevention shield")
    void absentTargetDoesNotResolve() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DazzlingReflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Only the first damage event is prevented, with no additional life gain")
    void secondDamageEventIsNotPrevented() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        castDazzlingReflection(pyromancer.getId());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);

        pyromancer.setTapped(false);
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private void castDazzlingReflection(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DazzlingReflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Card creatureWithPower(int power) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        return card;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
