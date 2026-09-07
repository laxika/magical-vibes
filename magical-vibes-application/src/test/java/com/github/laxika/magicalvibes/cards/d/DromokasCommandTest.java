package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DromokasCommand.class, GhostlyPrison.class, GrizzlyBears.class,
        HillGiant.class, LightningBolt.class})
class DromokasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Prevention and counter modes prevent spell damage and add a counter")
    void preventsInstantOrSorceryDamageAndAddsCounter() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LightningBolt bolt = new LightningBolt();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new DromokasCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, bolt.getId(),
                List.of(counterTarget.getId()));
        harness.passBothPriorities();

        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrifice mode makes the targeted player sacrifice an enchantment")
    void targetedPlayerSacrificesAnEnchantment() {
        harness.addToBattlefield(player2, new GhostlyPrison());
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DromokasCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 2}, null,
                List.of(player2.getId(), counterTarget.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fight mode uses a creature you control and an opposing creature")
    void fightsOpposingCreature() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DromokasCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{2, 3}, null,
                List.of(counterTarget.getId(), fighter.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fighter);
    }

    @Test
    @DisplayName("Prevention mode rejects a non-spell target")
    void preventionModeRejectsNonSpellTarget() {
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new DromokasCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 2}, invalidTarget.getId(),
                List.of(counterTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
