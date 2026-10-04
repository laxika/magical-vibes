package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireforgersPuzzleknot.class, EagerConstruct.class, ChandraTorchOfDefiance.class})
class FireforgersPuzzleknotTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 1 damage to a target creature")
    void enteringBattlefieldDealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering the battlefield deals 1 damage to a target player")
    void enteringBattlefieldDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a target creature")
    void sacrificeAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new FireforgersPuzzleknot());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a target player")
    void sacrificeAbilityDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new FireforgersPuzzleknot());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
    }

    @Test
    void enteringBattlefieldDealsDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeAbilityDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new FireforgersPuzzleknot());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Fireforger's Puzzleknot");
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void enterTriggerStillDealsDamageAfterSacrificingSourceInResponse() {
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fireforger's Puzzleknot");
        harness.assertLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void bothAbilitiesCanTargetTheirController() {
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.assertLife(player1, 19);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
