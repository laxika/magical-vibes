package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationOccupation.class, Shock.class})
class FireNationOccupationTest extends BaseCardTest {

    @Test
    void enteringCreatesSoldierWithFirebending() {
        harness.setHand(player1, List.of(new FireNationOccupation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertSoldierWithFirebending();
    }

    @Test
    void castingSpellDuringOpponentsTurnCreatesAnotherSoldier() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertSoldierWithFirebending();
    }

    @Test
    void castingSpellDuringOwnTurnDoesNotCreateSoldier() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void SoldierFirebendingAddsRedManaUntilEndOfCombat() {
        castOccupation();
        Permanent soldier = findPermanent(player1, "Soldier");
        soldier.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(soldier)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void assertSoldierWithFirebending() {
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(soldier.getEffectivePower()).isEqualTo(2);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIREBENDING)).isTrue();
    }

    private void enterOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOccupation() {
        harness.setHand(player1, List.of(new FireNationOccupation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
