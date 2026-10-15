package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfRunes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConductiveCurrent.class, GrizzlyBears.class, Opt.class, Shock.class})
class ConductiveCurrentTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConductiveCurrent()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void chosenInstantPerpetuallyDealsTwoAdditionalNoncombatDamage() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new Shock(), new Opt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @CardUsed(WallOfRunes.class)
    void dealsOnlyThreeDamageAndDoesNotDamagePlayersWithoutAnEligibleHandCard() {
        var wall = harness.addToBattlefieldAndReturn(player2, new WallOfRunes());
        harness.setHand(player1, List.of(new ConductiveCurrent(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Runes");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotChooseACreatureCardOrDeclineTheChoice() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 1, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @CardUsed(WallOfRunes.class)
    void chosenSorceryDealsAdditionalDamageToEachCreature() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new ConductiveCurrent()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addToBattlefield(player1, new WallOfRunes());
        harness.addToBattlefield(player2, new WallOfRunes());
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall of Runes");
        harness.assertNotOnBattlefield(player2, "Wall of Runes");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(WallOfRunes.class)
    void chosenInstantDealsAdditionalDamageToAPermanent() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        var wall = harness.addToBattlefieldAndReturn(player2, new WallOfRunes());
        harness.castAndResolveInstant(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player2, "Wall of Runes");
    }

    @Test
    void repeatedGrantsStackOnTheSameCard() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new ConductiveCurrent(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 14);
    }

    @Test
    void leavesAnUnchosenCopyOfTheSameCardUnmodified() {
        harness.setHand(player1, List.of(new ConductiveCurrent(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.castAndResolveInstant(player1, 1, player2.getId());
        harness.assertLife(player2, 18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
    }

    @Test
    @CardUsed(Regrowth.class)
    void perpetualBonusSurvivesResolutionAndReturnFromTheGraveyard() {
        var shock = new Shock();
        harness.setHand(player1, List.of(new ConductiveCurrent(), shock, new Regrowth()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Shock");

        harness.castAndResolveSorcery(player1, 0, shock.getId());
        harness.assertInHand(player1, "Shock");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 12);
    }
}
