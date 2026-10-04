package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({FireNationOccupation.class, LightningBolt.class})
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
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertSoldierWithFirebending();
    }

    @Test
    void castingSpellDuringOwnTurnDoesNotCreateSoldier() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        harness.setHand(player1, List.of(new LightningBolt()));
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

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(soldier)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void opponentCastingSpellDoesNotCreateSoldier() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        enterOpponentsTurn();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    void eachSpellDuringOpponentsTurnCreatesSoldier() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    void castTriggerCreatesSoldierBeforeSpellResolves() {
        harness.addToBattlefield(player1, new FireNationOccupation());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        int opponentsLife = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentsLife);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentsLife - 3);
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
