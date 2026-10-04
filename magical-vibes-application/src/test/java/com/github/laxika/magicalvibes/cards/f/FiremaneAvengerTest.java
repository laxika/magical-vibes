package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.g.GideonChampionOfJustice;
import com.github.laxika.magicalvibes.cards.g.GrislySpectacle;
import com.github.laxika.magicalvibes.cards.s.ShieldedPassage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiremaneAvenger.class, DiscipleOfTheOldWays.class, GrislySpectacle.class,
        ShieldedPassage.class, GideonChampionOfJustice.class})
class FiremaneAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion deals 3 damage to target player and gains 3 life")
    void battalionDamagesPlayerAndGainsLife() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // 3 from the trigger, then 3 + 2 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Battalion deals 3 damage to target creature")
    void battalionDamagesTargetCreature() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent opposing = addCreatureReady(player2, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1, 2));

        harness.handlePermanentChosen(player1, opposing.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Disciple of the Old Ways");
        harness.assertInGraveyard(player2, "Disciple of the Old Ways");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void noTriggerWithTooFewAttackers() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Only the 3 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Battalion does not trigger when Firemane Avenger stays out of combat")
    void doesNotTriggerUnlessAvengerAttacks() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(1, 2, 3));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Battalion triggers only once with more than three attackers")
    void triggersOnceWithFourAttackers() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1, 2, 3));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Battalion can target its controller")
    void canDamageItsController() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Battalion can damage a planeswalker")
    void damagesTargetPlaneswalker() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonChampionOfJustice());
        gideon.setCounterCount(CounterType.LOYALTY, 4);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, gideon.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Battalion still resolves after another attacker dies")
    void resolvesAfterAnotherAttackerDies() {
        addCreatureReady(player1, new FiremaneAvenger());
        Permanent companion = addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));
        harness.castInstant(player2, 0, companion.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(companion);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Battalion still deals damage and gains life after its source dies")
    void resolvesAfterAvengerDies() {
        Permanent avenger = addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));
        harness.castInstant(player2, 0, avenger.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertInGraveyard(player1, "Firemane Avenger");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An illegal battalion target prevents both damage and life gain")
    void gainsNoLifeWhenTargetDiesBeforeResolution() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent target = addCreatureReady(player2, new DiscipleOfTheOldWays());
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Disciple of the Old Ways");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Preventing battalion damage does not prevent its life gain")
    void gainsLifeEvenWhenDamageIsPrevented() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent target = addCreatureReady(player2, new DiscipleOfTheOldWays());
        harness.setHand(player2, List.of(new ShieldedPassage()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        harness.castInstant(player2, 0, target.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 23);
    }
}
