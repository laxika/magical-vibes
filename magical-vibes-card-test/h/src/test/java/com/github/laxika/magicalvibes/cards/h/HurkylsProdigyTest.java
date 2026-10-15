package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HurkylsProdigy.class)
class HurkylsProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a tapped Powerstone token")
    void enteringCreatesTappedPowerstone() {
        harness.enterBattlefieldAndReturn(player1, new HurkylsProdigy());
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying on attack makes it unblockable and perpetually boosts its power")
    void payingOnAttackAppliesUnblockableAndPerpetualBoost() {
        Permanent prodigy = addReadyProdigy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasCantBeBlocked(gd, prodigy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, prodigy)).isFalse();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the attack payment does nothing")
    void decliningAttackPaymentDoesNothing() {
        Permanent prodigy = addReadyProdigy(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasCantBeBlocked(gd, prodigy)).isFalse();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
    }

    private Permanent addReadyProdigy(Player player) {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player, new HurkylsProdigy());
        prodigy.setSummoningSick(false);
        return prodigy;
    }
}
