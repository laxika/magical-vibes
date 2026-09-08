package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidIllusionist.class, Forest.class, GrizzlyBears.class, Shock.class})
class CephalidIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards when targeted by a spell")
    void millsWhenTargetedBySpell() {
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new CephalidIllusionist());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, illusionist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest", "Forest");
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and by the targeted creature")
    void preventsCombatDamageToAndByTargetCreature() {
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new CephalidIllusionist());
        illusionist.setSummoningSick(false);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new CephalidIllusionist());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(illusionist.isTapped()).isFalse();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner,
                                  com.github.laxika.magicalvibes.model.Player defender,
                                  int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(com.github.laxika.magicalvibes.model.Player owner,
                                 int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
