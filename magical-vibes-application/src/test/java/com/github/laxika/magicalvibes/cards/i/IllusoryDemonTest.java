package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IllusoryDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell triggers Illusory Demon's sacrifice ability")
    void castingSpellTriggersSacrifice() {
        harness.addToBattlefield(player1, new IllusoryDemon());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        // Opt on the stack plus Illusory Demon's triggered ability
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Illusory Demon"));
    }

    @Test
    @DisplayName("Resolving the trigger sacrifices Illusory Demon")
    void triggerSacrificesDemon() {
        harness.addToBattlefield(player1, new IllusoryDemon());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve the sacrifice trigger (on top of the stack)

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Illusory Demon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Illusory Demon"));
    }

    @Test
    @DisplayName("An opponent casting a spell does not sacrifice Illusory Demon")
    void opponentSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new IllusoryDemon());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);

        GameData gd = harness.getGameData();
        // Only Opt on the stack — no sacrifice trigger from the controller's Demon
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Illusory Demon"));
    }
}
