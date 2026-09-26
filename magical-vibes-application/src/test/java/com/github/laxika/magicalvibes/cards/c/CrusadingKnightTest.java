package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgonizingDemise;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TrenchWurm;
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

@CardUsed({AgonizingDemise.class, CrusadingKnight.class, Swamp.class, TrenchWurm.class})
class CrusadingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Crusading Knight is 2/2 without opponent Swamps")
    void baseStatsWithoutOpponentSwamps() {
        Permanent knight = addCreatureReady(player1, new CrusadingKnight());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Crusading Knight gets +1/+1 for each opponent Swamp")
    void countsOpponentSwamps() {
        Permanent knight = addCreatureReady(player1, new CrusadingKnight());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Crusading Knight cannot be targeted by black spells")
    void hasProtectionFromBlack() {
        Permanent knight = addCreatureReady(player2, new CrusadingKnight());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Crusading Knight cannot be blocked by black creatures")
    void blackCreatureCannotBlock() {
        Permanent knight = addCreatureReady(player1, new CrusadingKnight());
        Permanent blocker = addCreatureReady(player2, new TrenchWurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(knight)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Crusading Knight takes no combat damage from black creatures")
    void takesNoCombatDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new TrenchWurm());
        Permanent knight = addCreatureReady(player2, new CrusadingKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }
}
