package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.s.Smallpox;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PersonalSanctuary.class, RuneclawBear.class, Shock.class,
        ChandraTheFirebrand.class, Smallpox.class, SongOfTheDryads.class})
class PersonalSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the controller is prevented during the controller's turn")
    void preventsDamageDuringControllersTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage to the controller is not prevented during an opponent's turn")
    void doesNotPreventDamageDuringOpponentsTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Combat damage to the controller during an opponent's turn still goes through")
    void doesNotPreventCombatDamageOnOpponentsTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage to the controller's creatures is not prevented")
    void doesNotPreventDamageToOwnCreatures() {
        harness.addToBattlefield(player1, new PersonalSanctuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Damage from the controller's own spell is also prevented")
    void preventsSelfInflictedDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The controller's opponent is not protected during the controller's turn")
    void doesNotProtectOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Life loss during the controller's turn is not damage and is not prevented")
    void doesNotPreventLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Smallpox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Personal Sanctuary");
    }

    @Test
    @DisplayName("Damage from a permanent's activated ability is prevented")
    void preventsAbilityDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraTheFirebrand());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Personal Sanctuary stops preventing damage when Song of the Dryads removes its ability")
    void doesNotPreventDamageAfterLosingPrintedAbility() {
        harness.setLife(player1, 20);
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new PersonalSanctuary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, sanctuary.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}
