package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({DawnCharm.class, GrizzlyBears.class, Shock.class, Spellbook.class})
class DawnCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 prevents combat damage to players and creatures")
    void preventsAllCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setHand(player1, List.of(new DawnCharm()));
        addWhiteMana();

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Mode 1 grants a regeneration shield to the target creature")
    void regeneratesTargetCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DawnCharm()));
        addWhiteMana();

        harness.castInstant(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        bear.setMarkedDamage(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Mode 1 cannot target a noncreature permanent")
    void regenerateModeRejectsNoncreaturePermanent() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.setHand(player1, List.of(new DawnCharm()));
        addWhiteMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Mode 2 counters a spell that targets its controller")
    void countersSpellTargetingController() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new DawnCharm()));
        addWhiteMana(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, 2, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Mode 2 cannot target a spell that targets another player")
    void counterModeRejectsSpellTargetingAnotherPlayer() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new DawnCharm()));
        addWhiteMana(player2);

        harness.castInstant(player1, 0, player1.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 2, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("targets you");
    }

    private void addWhiteMana() {
        addWhiteMana(player1);
    }

    private void addWhiteMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
