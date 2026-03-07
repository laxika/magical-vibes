package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IchorExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a 2-power creature gives all creatures -2/-2")
    void sacrificeTwoPowerCreatureGivesMinusTwoMinusTwo() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        Permanent survivor = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(survivor);

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Grizzly Bears was sacrificed as cost
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Air Elemental (4/4) gets -2/-2, becoming effectively 2/2
        assertThat(survivor.getPowerModifier()).isEqualTo(-2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Sacrificing a 1-power creature gives all creatures -1/-1")
    void sacrificeOnePowerCreatureGivesMinusOneMinusOne() {
        Permanent sacrifice = new Permanent(new LlanowarElves()); // 1/1
        Permanent target1 = new Permanent(new GrizzlyBears()); // 2/2
        Permanent target2 = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player1.getId()).add(target1);
        gd.playerBattlefields.get(player2.getId()).add(target2);

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Both surviving creatures get -1/-1
        assertThat(target1.getPowerModifier()).isEqualTo(-1);
        assertThat(target1.getToughnessModifier()).isEqualTo(-1);
        assertThat(target2.getPowerModifier()).isEqualTo(-1);
        assertThat(target2.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Sacrificed creature's power includes +1/+1 counters")
    void sacrificedCreaturePowerIncludesCounters() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        sacrifice.setPlusOnePlusOneCounters(3); // becomes 5/5
        Permanent survivor = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(survivor);

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Grizzly Bears had effective power 5, so all creatures get -5/-5
        assertThat(survivor.getPowerModifier()).isEqualTo(-5);
        assertThat(survivor.getToughnessModifier()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Casting puts spell on stack with correct xValue")
    void castingPutsSpellOnStackWithCorrectXValue() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
