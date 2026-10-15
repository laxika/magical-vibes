package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TheDalekEmperor.class, ClockworkDroid.class, DalekDrone.class})
class TheDalekEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Daleks reduces the generic mana cost")
    void affinityForDaleksReducesGenericCost() {
        harness.addToBattlefield(player1, new DalekDrone());
        harness.setHand(player1, List.of(new TheDalekEmperor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Dalek Emperor");
        harness.assertNotInHand(player1, "The Dalek Emperor");
    }

    @Test
    @DisplayName("Other Daleks you control have haste")
    void otherDaleksHaveHaste() {
        Permanent emperor = harness.addToBattlefieldAndReturn(player1, new TheDalekEmperor());

        advanceToCombatAndResolve(player1);

        Permanent dalek = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(dalek.getCard().getSubtypes()).contains(CardSubtype.DALEK);
        assertThat(gqs.hasKeyword(gd, dalek, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, emperor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent can sacrifice a creature instead of giving you a Dalek")
    void opponentCanSacrificeCreature() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        harness.addToBattlefield(player2, new ClockworkDroid());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Clockwork Droid");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Declining the sacrifice creates a Dalek for the Emperor's controller")
    void decliningSacrificeCreatesDalek() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        harness.addToBattlefield(player2, new DalekDrone());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Dalek Drone");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
                    assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The Emperor does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Haste applies only to other Daleks controlled by the Emperor's controller")
    void hasteDoesNotApplyToOpposingDaleksOrOtherCreatureTypes() {
        Permanent emperor = harness.addToBattlefieldAndReturn(player1, new TheDalekEmperor());
        Permanent ownDalek = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        Permanent opposingDalek = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());

        assertThat(gqs.hasKeyword(gd, ownDalek, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, emperor, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingDalek, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonDalek, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent chooses which of multiple creatures to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertInGraveyard(player2, "Clockwork Droid");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
