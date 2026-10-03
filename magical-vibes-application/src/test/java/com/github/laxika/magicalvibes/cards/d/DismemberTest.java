package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dismember.class, MahamotiDjinn.class, GrizzlyBears.class, FountainOfYouth.class})
class DismemberTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dismember puts it on stack with target creature")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dismember");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Dismember gives -5/-5 to target creature")
    void resolvesAndDebuffsTarget() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent djinn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(djinn.getPowerModifier()).isEqualTo(-5);
        assertThat(djinn.getToughnessModifier()).isEqualTo(-5);
        assertThat(djinn.getEffectivePower()).isEqualTo(0);
        assertThat(djinn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dismember kills a creature with toughness 5 or less")
    void killsSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Debuff from Dismember wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent djinn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(djinn.getPowerModifier()).isEqualTo(0);
        assertThat(djinn.getToughnessModifier()).isEqualTo(0);
        assertThat(djinn.getEffectivePower()).isEqualTo(5);
        assertThat(djinn.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Dismember fizzles if target is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dismember")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // legal creature target so the spell is castable (CR 601.2c)
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Dismember can be cast with one generic mana and four life")
    void paysBothPhyrexianSymbolsWithLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertLife(player1, 16);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dismember");
    }

    @Test
    @DisplayName("Dismember can mix black mana and life for its Phyrexian symbols")
    void mixesManaAndLifePayments() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying all of Dismember's cost with mana does not cost life")
    void paysEntireCostWithMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot pay four life for Dismember while at three life")
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 3);
        harness.assertInHand(player1, "Dismember");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Two Dismembers cumulatively reduce toughness")
    void repeatedDebuffsAreCumulative() {
        harness.addToBattlefield(player2, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember(), new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        UUID targetId = harness.getPermanentId(player2, "Mahamoti Djinn");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mahamoti Djinn");
        harness.assertInGraveyard(player2, "Mahamoti Djinn");
    }
}
