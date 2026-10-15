package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Crystacean;
import com.github.laxika.magicalvibes.cards.r.RamThrough;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LurkingDeadeye.class, Crystacean.class, RamThrough.class})
class LurkingDeadeyeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a creature that was dealt damage this turn")
    void etbDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Crystacean());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crystacean");
        harness.assertInGraveyard(player2, "Crystacean");
    }

    @Test
    @DisplayName("ETB can destroy your own creature that was dealt damage this turn")
    void etbCanDestroyOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Crystacean());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystacean");
        harness.assertInGraveyard(player1, "Crystacean");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Crystacean());

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Can enter the battlefield when no creature was dealt damage this turn")
    void entersWithoutTargetWhenNoCreatureWasDamaged() {
        harness.castFromHand(player1, new LurkingDeadeye(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lurking Deadeye");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage history still permits destruction after marked damage is removed")
    void destroysCreatureAfterMarkedDamageIsRemoved() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Crystacean());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Crystacean());
        harness.setHand(player1, List.of(new RamThrough()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        target.setMarkedDamage(0);

        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crystacean");
        harness.assertNotOnBattlefield(player2, "Crystacean");
        harness.assertOnBattlefield(player1, "Crystacean");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void castsDuringOpponentsEndStep() {
        gd.activePlayerId = player2.getId();
        gd.currentStep = TurnStep.END_STEP;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Crystacean());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new LurkingDeadeye()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lurking Deadeye");
        harness.assertInGraveyard(player2, "Crystacean");
    }
}
