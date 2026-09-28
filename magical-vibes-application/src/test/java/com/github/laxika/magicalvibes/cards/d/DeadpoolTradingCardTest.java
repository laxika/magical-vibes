package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TidalWarrior;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadpoolTradingCard.class, TidalWarrior.class})
class DeadpoolTradingCardTest extends BaseCardTest {

    private Permanent castAndChoose(Permanent target, boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadpoolTradingCard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
        return findPermanent(player1, "Deadpool, Trading Card");
    }

    @Test
    @DisplayName("May exchange swaps rules text without changing creature identity")
    void exchangesTextBoxes() {
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player1, new TidalWarrior());

        Permanent deadpool = castAndChoose(tidalWarrior, true);

        assertThat(deadpool.getCard().getName()).isEqualTo("Deadpool, Trading Card");
        assertThat(tidalWarrior.getCard().getName()).isEqualTo("Tidal Warrior");
        assertThat(deadpool.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED)).isEmpty();
        assertThat(tidalWarrior.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED))
                .singleElement().isInstanceOf(LoseLifeEffect.class);
        assertThat(deadpool.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(tidalWarrior.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB choice leaves both text boxes unchanged")
    void declinesExchange() {
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player1, new TidalWarrior());

        Permanent deadpool = castAndChoose(tidalWarrior, false);

        assertThat(deadpool.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED))
                .singleElement().isInstanceOf(LoseLifeEffect.class);
        assertThat(tidalWarrior.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED)).isEmpty();
    }

    @Test
    @DisplayName("Copied Deadpool sacrifice ability makes each other player draw")
    void copiedSacrificeAbilityWorks() {
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player1, new TidalWarrior());
        castAndChoose(tidalWarrior, true);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int battlefieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tidalWarrior);
        harness.activateAbility(player1, battlefieldIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tidal Warrior");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
    }
}
