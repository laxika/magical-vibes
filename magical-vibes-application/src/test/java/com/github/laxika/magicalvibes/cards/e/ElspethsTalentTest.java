package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElspethsTalent.class, ChandraNalaar.class, GrizzlyBears.class})
class ElspethsTalentTest extends BaseCardTest {

    @Test
    void enchantedPlaneswalkerGetsSoldierTokenLoyaltyAbility() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ElspethsTalent());
        talent.setAttachedTo(chandra.getId());

        int chandraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chandra);
        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, chandra).size() - 1;
        harness.activateAbility(player1, chandraIndex, grantedAbilityIndex, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void loyaltyActivationPumpsOwnCreaturesAndGrantsVigilanceUntilEndOfTurn() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ElspethsTalent());
        talent.setAttachedTo(chandra.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0,
                null, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void cannotEnchantNonPlaneswalker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElspethsTalent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a planeswalker");
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(chandra);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }
}
