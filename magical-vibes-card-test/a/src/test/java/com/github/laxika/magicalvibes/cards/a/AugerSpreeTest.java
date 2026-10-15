package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AugerSpree.class, GrizzlyBears.class, ShivanDragon.class})
class AugerSpreeTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new AugerSpree()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Gives target creature +4/-4")
    void appliesBoost() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon()); // 5/5
        castOn(dragon);

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        assertThat(dragon.getToughnessModifier()).isEqualTo(-4);
        assertThat(dragon.getEffectivePower()).isEqualTo(9);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-4 toughness kills a small creature")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        castOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("+4/-4 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        castOn(dragon);
        assertThat(dragon.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();
        harness.setHand(player1, List.of(new AugerSpree()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can give an opponent's creature +4/-4 without affecting other creatures")
    void boostsOpponentsCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());

        castOn(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getEffectivePower()).isEqualTo(9);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Auger Spree");
    }

    @Test
    @DisplayName("Reduced toughness makes previously marked damage lethal")
    void killsCreatureWithPreviouslyMarkedDamage() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        dragon.setMarkedDamage(1);

        castOn(dragon);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dragon);
        harness.assertInGraveyard(player2, "Shivan Dragon");
    }
}
