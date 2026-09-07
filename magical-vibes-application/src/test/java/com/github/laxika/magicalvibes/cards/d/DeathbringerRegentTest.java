package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathbringerRegent.class, GrizzlyBears.class, BeaconOfUnrest.class})
class DeathbringerRegentTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand with five other creatures, it destroys all other creatures")
    void castFromHandWithFiveOtherCreaturesDestroysAllOtherCreatures() {
        addBears(player1, 2);
        addBears(player2, 3);

        castFromHand();

        harness.assertOnBattlefield(player1, "Deathbringer Regent");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("It does not trigger with only four other creatures")
    void doesNotTriggerWithOnlyFourOtherCreatures() {
        addBears(player1, 2);
        addBears(player2, 2);

        castFromHand();

        harness.assertOnBattlefield(player1, "Deathbringer Regent");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The five-creature condition is checked again when the ability resolves")
    void conditionIsCheckedAgainOnResolution() {
        List<Permanent> bears = new ArrayList<>();
        bears.addAll(addBears(player1, 2));
        bears.addAll(addBears(player2, 3));

        harness.setHand(player1, List.of(new DeathbringerRegent()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(bears.getLast());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deathbringer Regent");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("It does not trigger when it enters from a graveyard")
    void doesNotTriggerWhenEnteringFromGraveyard() {
        addBears(player1, 2);
        addBears(player2, 3);

        harness.setGraveyard(player1, List.of(new DeathbringerRegent()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deathbringer Regent");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private List<Permanent> addBears(Player player, int count) {
        List<Permanent> bears = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            bears.add(harness.addToBattlefieldAndReturn(player, new GrizzlyBears()));
        }
        return bears;
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new DeathbringerRegent()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
