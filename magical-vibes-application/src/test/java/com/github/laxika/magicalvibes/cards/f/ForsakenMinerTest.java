package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForsakenMiner.class, Shock.class})
class ForsakenMinerTest extends BaseCardTest {

    @Test
    @DisplayName("Pays black mana to return from the graveyard after a crime")
    void paysToReturnFromGraveyard() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(miner.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(miner.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves it in the graveyard")
    void decliningPaymentLeavesMinerInGraveyard() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(miner.getId()));
    }

    @Test
    @DisplayName("Targeting yourself does not trigger the graveyard ability")
    void targetingYourselfDoesNotTriggerAbility() {
        ForsakenMiner miner = new ForsakenMiner();
        harness.setGraveyard(player1, List.of(miner));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(miner);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
