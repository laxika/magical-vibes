package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.Atogatog;
import com.github.laxika.magicalvibes.cards.a.Auramancer;
import com.github.laxika.magicalvibes.cards.k.KirtarsDesire;
import com.github.laxika.magicalvibes.cards.o.OtarianJuggernaut;
import com.github.laxika.magicalvibes.cards.r.Repel;
import com.github.laxika.magicalvibes.cards.s.SecondThoughts;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarnestFellowship.class, Auramancer.class, OtarianJuggernaut.class,
        Atogatog.class, Repel.class, SecondThoughts.class, KirtarsDesire.class})
class EarnestFellowshipTest extends BaseCardTest {

    @Test
    @DisplayName("A creature cannot block a creature sharing its color")
    void coloredCreatureCannotBlockSameColorCreature() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent attacker = addCreatureReady(player1, new Auramancer());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Auramancer());

        prepareDeclareBlockers(player1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A colorless creature can block a colored creature")
    void colorlessCreatureCanBlockColoredCreature() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent attacker = addCreatureReady(player1, new Auramancer());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new OtarianJuggernaut());

        prepareDeclareBlockers(player1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A multicolored creature cannot be targeted by spells of either color")
    void multicoloredCreatureHasProtectionFromEachColor() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent target = addCreatureReady(player2, new Atogatog());
        target.setAttacking(true);

        harness.setHand(player1, List.of(new Repel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");

        harness.setHand(player1, List.of(new SecondThoughts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection prevents combat damage from a creature's own color")
    void preventsDamageFromOwnColor() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent attacker = addCreatureReady(player1, new Auramancer());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Auramancer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A colorless creature can be targeted by a colored spell")
    void colorlessCreatureHasNoProtectionFromColors() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent target = addCreatureReady(player2, new OtarianJuggernaut());

        harness.setHand(player1, List.of(new Repel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Otarian Juggernaut");
    }

    @Test
    @DisplayName("A creature cannot be enchanted by an Aura sharing its color")
    void coloredCreatureCannotBeEnchantedBySameColorAura() {
        harness.addToBattlefield(player1, new EarnestFellowship());
        Permanent target = addCreatureReady(player2, new Auramancer());

        harness.setHand(player1, List.of(new KirtarsDesire()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }
}
