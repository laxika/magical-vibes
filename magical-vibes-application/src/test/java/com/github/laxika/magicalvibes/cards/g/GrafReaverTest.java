package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrafReaver.class, GrizzlyBears.class, GarrukWildspeaker.class})
class GrafReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Graf Reaver and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exploiting a creature destroys a target planeswalker")
    void exploitDestroysTargetPlaneswalker() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Exploit cannot target a creature")
    void exploitCannotTargetCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).doesNotContain(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Graf Reaver deals 1 damage to its controller during their upkeep")
    void dealsDamageDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new GrafReaver());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Graf Reaver does not deal damage during an opponent's upkeep")
    void doesNotDealDamageDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GrafReaver());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void castGrafReaverToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrafReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
