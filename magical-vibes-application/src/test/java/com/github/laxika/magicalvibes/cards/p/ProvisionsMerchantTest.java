package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProvisionsMerchant.class, GrizzlyBears.class})
class ProvisionsMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void createsFoodWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new ProvisionsMerchant());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Sacrificing a Food boosts attacking creatures and gives them trample")
    void sacrificingFoodBoostsAttackingCreatures() {
        Permanent merchant = harness.enterBattlefieldAndReturn(player1, new ProvisionsMerchant());
        harness.passBothPriorities();
        merchant.setSummoningSick(false);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent food = findPermanent(player1, "Food");

        declareAttackers(player1, java.util.List.of(battlefieldIndex(merchant), battlefieldIndex(attacker)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(merchant.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merchant, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merchant.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merchant, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Declining the Food sacrifice leaves attacking creatures unchanged")
    void decliningFoodSacrificeDoesNothing() {
        Permanent merchant = harness.enterBattlefieldAndReturn(player1, new ProvisionsMerchant());
        harness.passBothPriorities();
        merchant.setSummoningSick(false);

        declareAttackers(player1, java.util.List.of(battlefieldIndex(merchant)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(merchant.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, merchant, Keyword.TRAMPLE)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
