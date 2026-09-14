package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CephalidIllusionist.class, GrizzlyBears.class, Shock.class})
class CephalidIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards when targeted by a spell")
    void millsWhenTargetedBySpell() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, illusionist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Mills three cards when targeted by an ability")
    void millsWhenTargetedByAbility() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        activateAbility(illusionist, illusionist);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and by the targeted creature")
    void preventsCombatDamageToAndByTargetCreature() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 3, 3,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        activateAbility(illusionist, attacker);
        resolveCombat();

        assertThat(illusionist.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the targeted creature to a player")
    void preventsCombatDamageDealtByTargetCreature() {
        harness.setLife(player2, 20);
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        Permanent attacker = addAttacker(player1, player2, 3, 3);

        activateAbility(illusionist, attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage only, not noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        Permanent target = addAttacker(player1, player2, 3, 3);

        activateAbility(illusionist, target);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(illusionist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires the full printed activation cost")
    void requiresPrintedActivationCost() {
        Permanent illusionist = addCreatureReady(player1, new CephalidIllusionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(illusionist);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(illusionist.isTapped()).isFalse();
    }

    private void activateAbility(Permanent source, Permanent target) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner,
                                  Player defender,
                                  int power, int toughness) {
        Permanent perm = addCreatureReady(owner, new GrizzlyBears());
        perm.setPowerModifier(power - 2);
        perm.setToughnessModifier(toughness - 2);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addBlocker(Player owner,
                                 int power, int toughness, int blockedAttackerIndex) {
        Permanent perm = addCreatureReady(owner, new GrizzlyBears());
        perm.setPowerModifier(power - 2);
        perm.setToughnessModifier(toughness - 2);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        return perm;
    }
}
