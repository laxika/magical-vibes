package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusoryDemon.class, Opt.class})
class IllusoryDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell triggers Illusory Demon's sacrifice ability")
    void castingSpellTriggersSacrifice() {
        harness.addToBattlefield(player1, new IllusoryDemon());
        harness.castFromHand(player1, new Opt(), "{U}");

        GameData gd = harness.getGameData();
        // Opt on the stack plus Illusory Demon's triggered ability
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Illusory Demon"));
    }

    @Test
    @DisplayName("Resolving the trigger sacrifices Illusory Demon")
    void triggerSacrificesDemon() {
        harness.addToBattlefield(player1, new IllusoryDemon());
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities(); // resolve the sacrifice trigger (on top of the stack)

        harness.assertNotOnBattlefield(player1, "Illusory Demon");
        harness.assertInGraveyard(player1, "Illusory Demon");
    }

    @Test
    @DisplayName("An opponent casting a spell does not sacrifice Illusory Demon")
    void opponentSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new IllusoryDemon());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Opt(), "{U}");

        GameData gd = harness.getGameData();
        // Only Opt on the stack — no sacrifice trigger from the controller's Demon
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertOnBattlefield(player1, "Illusory Demon");
    }

    @Test
    @DisplayName("Casting Illusory Demon does not trigger its own sacrifice ability")
    void castingDemonDoesNotTriggerItself() {
        harness.castFromHand(player1, new IllusoryDemon(), "{1}{U}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Illusory Demon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a creature sacrifices the existing Demon before the creature resolves")
    void creatureSpellTriggersSacrificeBeforeResolving() {
        IllusoryDemon original = new IllusoryDemon();
        IllusoryDemon castDemon = new IllusoryDemon();
        harness.addToBattlefield(player1, original);

        harness.castFromHand(player1, castDemon, "{1}{U}{B}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Illusory Demon");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(castDemon);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Illusory Demon").getCard()).isSameAs(castDemon);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Demon sacrifices itself when its controller casts a spell")
    void multipleDemonsSacrificeIndependently() {
        IllusoryDemon first = new IllusoryDemon();
        IllusoryDemon second = new IllusoryDemon();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);

        harness.castFromHand(player1, new IllusoryDemon(), "{1}{U}{B}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Illusory Demon")).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Illusory Demon");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).hasSize(1);
    }
}
