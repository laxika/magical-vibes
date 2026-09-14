package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonkeyCage.class, FreshVolunteers.class, Memnite.class, Disenchant.class})
class MonkeyCageTest extends BaseCardTest {

    @Test
    @DisplayName("Creature entry sacrifices the Cage and creates one Monkey per mana value")
    void creatureEntryCreatesTokensEqualToManaValue() {
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        assertThat(findPermanents(player1, "Monkey")).hasSize(2);
        assertThat(findPermanents(player1, "Monkey")).allSatisfy(monkey -> {
            assertThat(monkey.getCard().isToken()).isTrue();
            assertThat(monkey.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(monkey.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(monkey.getCard().getSubtypes()).containsExactly(CardSubtype.MONKEY);
            assertThat(monkey.getCard().getPower()).isEqualTo(2);
            assertThat(monkey.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Zero-mana creature entry sacrifices the Cage without creating tokens")
    void zeroManaCreatureCreatesNoTokens() {
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.castFromHand(player1, new Memnite(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        assertThat(findPermanents(player1, "Monkey")).isEmpty();
    }

    @Test
    @DisplayName("A creature entering under an opponent's control triggers the Cage")
    void opponentCreatureEntryCreatesTokensUnderCageControllersControl() {
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(findPermanents(player1, "Monkey")).hasSize(2);
        assertThat(findPermanents(player2, "Monkey")).isEmpty();
    }

    @Test
    @DisplayName("Removing the Cage before its trigger resolves prevents token creation")
    void noTokensIfCageLeavesBeforeTriggerResolves() {
        Permanent cage = harness.addToBattlefieldAndReturn(player1, new MonkeyCage());
        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, cage.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        harness.assertInGraveyard(player1, "Disenchant");
        assertThat(findPermanents(player1, "Monkey")).isEmpty();
    }
}
