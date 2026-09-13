package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BubblingMuck;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitteringHorror.class, MetathranSoldier.class, BubblingMuck.class})
class SkitteringHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell triggers Skittering Horror's sacrifice ability")
    void castingCreatureSpellTriggersSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Skittering Horror"));
    }

    @Test
    @DisplayName("Resolving the trigger sacrifices Skittering Horror")
    void triggerSacrificesHorror() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skittering Horror");
        harness.assertInGraveyard(player1, "Skittering Horror");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Skittering Horror")
    void castingNoncreatureSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new BubblingMuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.assertOnBattlefield(player1, "Skittering Horror");
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Skittering Horror")
    void opponentCreatureSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MetathranSoldier()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        harness.assertOnBattlefield(player1, "Skittering Horror");
    }
}
