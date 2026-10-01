package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodKnight.class, Calciderm.class, DawnCharm.class})
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
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
        assertThat(knight.getMarkedDamage()).isZero();
    }
}
