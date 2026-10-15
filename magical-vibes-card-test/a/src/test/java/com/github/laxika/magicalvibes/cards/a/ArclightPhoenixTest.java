package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArclightPhoenix.class, Shock.class, GrizzlyBears.class, DirectCurrent.class})
class ArclightPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard after casting three instant spells")
    void returnsAfterThreeInstantSpells() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Does not return before three matching spells have been cast")
    void doesNotReturnBeforeThreshold() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Counts only instant and sorcery spells")
    void countsOnlyInstantAndSorcerySpells() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Returns after a mixture of instant and sorcery spells")
    void returnsAfterMixedSpellTypes() {
        harness.setGraveyard(player1, List.of(new ArclightPhoenix()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new Shock(), new DirectCurrent(), new Shock()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arclight Phoenix");
        harness.assertNotInGraveyard(player1, "Arclight Phoenix");
    }

    @Test
    @DisplayName("Does not return during the opponent's combat even after casting three spells")
    void doesNotReturnDuringOpponentsCombat() {
        harness.setGraveyard(player1, List.of(new ArclightPhoenix()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.assertInGraveyard(player1, "Arclight Phoenix");
        harness.assertNotOnBattlefield(player1, "Arclight Phoenix");
    }

    @Test
    @DisplayName("The opponent's spells do not count toward the threshold")
    void opponentsSpellsDoNotCount() {
        harness.setGraveyard(player1, List.of(new ArclightPhoenix()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.assertInGraveyard(player1, "Arclight Phoenix");
        harness.assertNotOnBattlefield(player1, "Arclight Phoenix");
    }

    @Test
    @DisplayName("Each Phoenix returns after more than three spells, including spells cast before it entered the graveyard")
    void returnsEachPhoenixAfterFourEarlierSpells() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        ArclightPhoenix first = new ArclightPhoenix();
        ArclightPhoenix second = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(first, second));
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        harness.assertNotInGraveyard(player1, "Arclight Phoenix");
    }

    @Test
    @DisplayName("Casting the third spell after combat begins does not trigger the return")
    void thirdSpellDuringCombatIsTooLate() {
        harness.setGraveyard(player1, List.of(new ArclightPhoenix()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Arclight Phoenix");
        harness.assertNotOnBattlefield(player1, "Arclight Phoenix");
    }
}
