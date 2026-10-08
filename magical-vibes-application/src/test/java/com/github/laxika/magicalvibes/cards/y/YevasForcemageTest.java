package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
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

@CardUsed({YevasForcemage.class, TimberpackWolf.class})
class YevasForcemageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+2")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new YevasForcemage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Yeva's Forcemage");

        Permanent wolf = findPermanent(player2, "Timberpack Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new YevasForcemage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Timberpack Wolf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Timberpack Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new YevasForcemage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player2, "Timberpack Wolf");
        assertThat(wolf.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new YevasForcemage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature — ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // ETB fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Entering alone requires targeting itself and gives itself +2/+2")
    void enteringAloneBoostsItself() {
        harness.castFromHand(player1, new YevasForcemage(), "{2}{G}");
        harness.passBothPriorities();

        Permanent forcemage = findPermanent(player1, "Yeva's Forcemage");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, forcemage.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, forcemage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forcemage)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trigger resolves even if Yeva's Forcemage leaves the battlefield")
    void triggerResolvesAfterSourceDies() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new YevasForcemage()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, wolf.getId());
        harness.passBothPriorities();

        Permanent forcemage = findPermanent(player1, "Yeva's Forcemage");
        forcemage.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Yeva's Forcemage");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering without being cast still triggers the boost")
    void enteringWithoutCastingTriggersBoost() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.enterBattlefieldAndReturn(player1, new YevasForcemage());

        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }
}
