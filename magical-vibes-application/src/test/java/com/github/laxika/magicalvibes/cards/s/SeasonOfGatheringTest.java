package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DemonicPact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfGathering.class, GrizzlyBears.class, HillGiant.class, Plains.class,
        UrzasBauble.class, DemonicPact.class})
class SeasonOfGatheringTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode chooses a creature during resolution and grants both keywords")
    void firstModeChoosesCreatureDuringResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        cast(mode(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant.getGrantedKeywords()).contains(Keyword.VIGILANCE, Keyword.TRAMPLE);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The first mode can be chosen twice")
    void firstModeCanBeChosenTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(mode(0, 0));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The second mode chooses artifact or enchantment during resolution")
    void secondModeDestroysChosenPermanentType() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new UrzasBauble());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new UrzasBauble());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new DemonicPact());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());

        cast(mode(1));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Destroy all artifacts");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentEnchantment);
    }

    @Test
    @DisplayName("The third mode draws cards equal to greatest controlled creature power")
    void thirdModeDrawsGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        cast(mode(2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private void cast(int selection) {
        harness.setHand(player1, List.of(new SeasonOfGathering()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, selection);
        harness.passBothPriorities();
    }

    private static int mode(int... modeIndices) {
        return ChooseOneEffect.encodeBudgetedModeSelection(5, List.of(1, 2, 3), modeIndices);
    }
}
