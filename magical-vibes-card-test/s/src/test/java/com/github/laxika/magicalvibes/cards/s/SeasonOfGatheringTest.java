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
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Choosing the first mode requires selecting a creature when creatures are available")
    void creatureChoiceCannotBeDeclined() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        cast(mode(0));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated first modes can choose different creatures independently")
    void repeatedFirstModesChooseDifferentCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(mode(0, 0));

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.VIGILANCE, Keyword.TRAMPLE);
        assertThat(giant.getGrantedKeywords()).contains(Keyword.VIGILANCE, Keyword.TRAMPLE);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("All five pawprints may be spent on the first mode")
    void firstModeCanBeChosenFiveTimes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(mode(0, 0, 0, 0, 0));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Repeated second modes can destroy artifacts and then enchantments")
    void secondModeCanChooseDifferentTypesEachTime() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new UrzasBauble());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new UrzasBauble());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new DemonicPact());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        cast(mode(1, 1));

        harness.handleListChoice(player1, "Destroy all artifacts");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownArtifact).contains(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact).contains(opponentEnchantment);
        harness.handleListChoice(player1, "Destroy all enchantments");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(opponentEnchantment);
    }

    @Test
    @DisplayName("First modes increase the power used by the draw mode regardless of selection order")
    void countersResolveBeforeDrawing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));
        cast(mode(2, 0, 0));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw mode ignores opposing creatures and uses the greatest own power")
    void drawModeUsesGreatestOwnCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains()));
        cast(mode(2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("First and draw modes do nothing when no own creatures exist")
    void creatureModesWithNoControlledCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Plains()));
        cast(mode(0, 2));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE, Keyword.TRAMPLE);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Season of Gathering may be cast without choosing any modes")
    void noModesMayBeChosen() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains()));
        cast(mode());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE, Keyword.TRAMPLE);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Season of Gathering");
    }

    @Test
    @DisplayName("The counter remains after the granted vigilance and trample expire")
    void keywordsExpireButCounterRemains() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(mode(0));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The first mode cannot choose an opponent's creature")
    void firstModeRejectsOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(mode(0));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void cast(int selection) {
        harness.setHand(player1, List.of(new SeasonOfGathering()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, selection);
    }

    private static int mode(int... modeIndices) {
        return ChooseOneEffect.encodeBudgetedModeSelection(5, List.of(1, 2, 3), modeIndices);
    }
}
