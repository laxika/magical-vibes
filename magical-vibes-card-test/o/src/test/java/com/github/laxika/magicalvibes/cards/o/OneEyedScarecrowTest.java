package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.c.CobbledWings;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneEyedScarecrow.class, BattlegroundGeist.class, WalkingCorpse.class, CobbledWings.class})
class OneEyedScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's flying creature gets -1/-0")
    void debuffsOpponentFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent geist = harness.addToBattlefieldAndReturn(player2, new BattlegroundGeist());

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's non-flying creature is not affected")
    void doesNotDebuffOpponentNonFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        // 2/2 base, unaffected
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own flying creature is not affected")
    void doesNotDebuffOwnFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new BattlegroundGeist());

        // 3/3 base, unaffected by own Scarecrow
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not affect itself")
    void doesNotAffectItself() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new OneEyedScarecrow());

        // 2/3 base, no self-debuff
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(3);
    }

    @Test
    @DisplayName("One-Eyed Scarecrow cannot attack even when ready")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new OneEyedScarecrow());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("defender");
    }

    @Test
    @DisplayName("Two Scarecrows give -2/-0 to opponent's flying creature")
    void twoScarecrowsStack() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent geist = harness.addToBattlefieldAndReturn(player2, new BattlegroundGeist());

        // 3/3 base - 2/0 from two Scarecrows = 1/3
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Debuff is removed when Scarecrow leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent geist = harness.addToBattlefieldAndReturn(player2, new BattlegroundGeist());

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("One-Eyed Scarecrow"));

        // Back to base 3/3
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }
    @Test
    @DisplayName("Debuff follows flying granted by equipment and ends when equipment leaves")
    void debuffFollowsGrantedFlying() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent wings = harness.addToBattlefieldAndReturn(player2, new CobbledWings());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        wings.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(wings);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple penalties can reduce power below zero without changing toughness")
    void powerCanBecomeNegative() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent wings = harness.addToBattlefieldAndReturn(player2, new CobbledWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
