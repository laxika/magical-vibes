package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vengevine.class, GrizzlyBears.class, Spellbook.class})
class VengevineTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard on the second creature spell, ignoring noncreature spells")
    void returnsOnSecondCreatureSpell() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vengevine");
        harness.assertNotInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("The first creature spell does not trigger Vengevine")
    void firstCreatureSpellDoesNotTrigger() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("Declining the trigger keeps Vengevine in the graveyard")
    void declineKeepsVengevineInGraveyard() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("A creature cast before Vengevine enters the graveyard still counts")
    void countsCreatureCastBeforeEnteringGraveyard() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new Vengevine()));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Vengevine");
        harness.assertNotInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("The third creature spell does not trigger after declining the second")
    void thirdCreatureSpellDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new Vengevine()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.withAutoStop(gd.currentStep, () -> harness.handleMayAbilityChosen(player1, false));
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vengevine");
        harness.assertNotOnBattlefield(player1, "Vengevine");
    }

    @Test
    @DisplayName("Vengevine entering the graveyard after the second creature spell does not trigger retroactively")
    void enteringGraveyardAfterSecondCastDoesNotTrigger() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.setGraveyard(player1, List.of(new Vengevine()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vengevine");
        harness.assertNotOnBattlefield(player1, "Vengevine");
    }

    @Test
    @DisplayName("Casting two creatures does not return an opponent's Vengevine")
    void doesNotTriggerOpponentsVengevine() {
        harness.setGraveyard(player2, List.of(new Vengevine()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Vengevine");
        harness.assertNotOnBattlefield(player2, "Vengevine");
        harness.assertNotOnBattlefield(player1, "Vengevine");
    }

    @Test
    @DisplayName("Vengevine returns before the second creature spell resolves")
    void returnsBeforeSecondCreatureResolves() {
        harness.setGraveyard(player1, List.of(new Vengevine()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.castFromHand(player1, secondCreature, "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(gd.currentStep, () -> harness.handleMayAbilityChosen(player1, true));

        harness.assertOnBattlefield(player1, "Vengevine");
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getId()).isEqualTo(secondCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneSatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(secondCreature.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(secondCreature.getId()));
    }

    @Test
    @DisplayName("Vengevine can attack on the turn it enters the battlefield")
    void canAttackImmediately() {
        harness.castFromHand(player1, new Vengevine(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }
}
