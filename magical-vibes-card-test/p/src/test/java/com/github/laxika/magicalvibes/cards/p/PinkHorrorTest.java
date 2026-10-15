package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PinkHorror.class, DarkRitual.class, DoomBlade.class, BlasphemousAct.class})
class PinkHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a chosen target when you cast an instant")
    void instantCastTriggerDealsDamage() {
        harness.addToBattlefield(player1, new PinkHorror());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new DarkRitual(), "{B}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("When it dies, creates two Blue Horror tokens with the spell-cast ability")
    void deathCreatesBlueHorrors() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blue Horror")).hasSize(2);
        assertThat(findPermanents(player1, "Pink Horror")).isEmpty();
    }

    @Test
    @DisplayName("Blue Horrors each deal 1 damage when you cast an instant")
    void blueHorrorsTriggerOnInstantCast() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sorcery cast trigger resolves before the sorcery kills Pink Horror")
    void sorceryTriggersBeforeResolution() {
        harness.addToBattlefield(player1, new PinkHorror());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new BlasphemousAct(), "{8}{R}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Pink Horror");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pink Horror");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Blue Horror")).hasSize(2);
    }

    @Test
    @DisplayName("Blue Horrors can choose different targets for a sorcery cast")
    void blueHorrorsChooseIndependentTargetsForSorcery() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new BlasphemousAct(), "{8}{R}");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Blue Horror")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Pink Horror")
    void opponentInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new PinkHorror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Coruscating Flames can damage a creature")
    void instantTriggerCanTargetCreature() {
        harness.addToBattlefield(player1, new PinkHorror());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PinkHorror());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Pink Horror");
    }

    @Test
    @DisplayName("Casting a creature does not trigger Pink Horror or Blue Horrors")
    void creatureSpellDoesNotTrigger() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new PinkHorror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new PinkHorror(), "{3}{U}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Pink Horror")).hasSize(2);
        assertThat(findPermanents(player1, "Blue Horror")).hasSize(2);
    }

    private void killPinkHorror(Permanent pinkHorror) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, pinkHorror.getId());
    }
}
