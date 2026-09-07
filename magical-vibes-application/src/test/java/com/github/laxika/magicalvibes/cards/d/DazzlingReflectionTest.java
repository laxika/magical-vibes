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
        Permanent target = addReady(player2, new GrizzlyBears());

        castDazzlingReflection(target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Prevents the target creature's next noncombat damage")
    void preventsTargetCreaturesNextNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent pyromancer = addReady(player2, new ProdigalPyromancer());
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
        Permanent attacker = addReady(player2, creatureWithPower(4));
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
        Permanent target = addReady(player2, creatureWithPower(4));
        Permanent other = addReady(player2, creatureWithPower(2));
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

    private void castDazzlingReflection(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DazzlingReflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
