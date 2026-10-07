package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunscorchedDesert.class, GideonOfTheTrials.class, ThoseWhoServe.class})
class SunscorchedDesertTest extends BaseCardTest {

    @Test
    @DisplayName("Entering deals 1 damage to the chosen opponent")
    void etbDamagesChosenOpponent() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        // The land is played (not cast), so its mandatory ETB target is chosen as the ability
        // goes on the stack.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Any player is a legal target — the controller may be chosen")
    void canTargetController() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A planeswalker is a legal target, a creature is not; damage removes a loyalty counter")
    void etbDamagesPlaneswalkerNotCreature() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());

        harness.setHand(player1, List.of(new SunscorchedDesert()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(planeswalker.getId())
                .doesNotContain(creature.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new SunscorchedDesert());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger still deals damage after the land leaves")
    void triggerResolvesWithoutSource() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.setLife(player2, 20);
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.setGraveyard(player1, List.of(land.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An entry trigger with a departed planeswalker target does not damage its controller")
    void departedTargetDoesNotRedirectDamage() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, planeswalker.getId());

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.setGraveyard(player2, List.of(planeswalker.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly played land can tap for mana after its entry trigger resolves")
    void newlyPlayedLandCanProduceMana() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
