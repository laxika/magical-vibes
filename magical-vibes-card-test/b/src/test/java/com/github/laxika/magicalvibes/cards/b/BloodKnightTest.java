package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.cards.m.MantleOfLeadership;
import com.github.laxika.magicalvibes.cards.r.RiptidePilferer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodKnight.class, Calciderm.class, DawnCharm.class, MantleOfLeadership.class,
        RiptidePilferer.class})
class BloodKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Blood Knight has protection from white")
    void hasProtectionFromWhite() {
        Permanent knight = addCreatureReady(player1, new BloodKnight());

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Blood Knight cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BloodKnight());
        harness.setHand(player1, List.of(new DawnCharm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(knight.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Blood Knight cannot be blocked by a white creature")
    void cannotBeBlockedByWhiteCreature() {
        Permanent blocker = addCreatureReady(player2, new Calciderm());
        Permanent knight = addCreatureReady(player1, new BloodKnight());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knight);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blood Knight prevents combat damage from a white creature")
    void preventsCombatDamageFromWhiteCreature() {
        Permanent attacker = addCreatureReady(player2, new Calciderm());
        Permanent knight = addCreatureReady(player1, new BloodKnight());
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knight);

        declareAttackersAndPrepareBlockers(player2, List.of(attackerIndex));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blood Knight kills a nonwhite blocker before it deals combat damage")
    void firstStrikeKillsNonwhiteBlockerBeforeItDealsDamage() {
        Permanent knight = addCreatureReady(player1, new BloodKnight());
        Permanent blocker = addCreatureReady(player2, new RiptidePilferer());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knight);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Blood Knight");
        harness.assertInGraveyard(player2, "Riptide Pilferer");
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blood Knight cannot be targeted by its controller's white Aura")
    void cannotBeTargetedByOwnWhiteAura() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BloodKnight());
        harness.setHand(player1, List.of(new MantleOfLeadership()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
        harness.assertInHand(player1, "Mantle of Leadership");
    }
}
