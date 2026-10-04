package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenePollinator.class, Forest.class})
class GenePollinatorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and another permanent, then adds one mana of the chosen color")
    void tapsItselfAndAnotherPermanentForMana() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);
        harness.activateAbility(player1, pollinatorIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(pollinator.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prompts which untapped permanent to tap when multiple are available")
    void promptsForPermanentChoice() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);
        harness.activateAbility(player1, pollinatorIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstForest.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pollinator.isTapped()).isTrue();
        assertThat(firstForest.isTapped()).isTrue();
        assertThat(secondForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another untapped permanent")
    void cannotActivateWithoutAnotherUntappedPermanent() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);

        assertThatThrownBy(() -> harness.activateAbility(player1, pollinatorIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("Can tap a summoning-sick creature for the additional cost")
    void canTapSummoningSickSupportCreature() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent support = harness.addToBattlefieldAndReturn(player1, new GenePollinator());
        support.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(pollinator.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while Gene Pollinator has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent pollinator = harness.addToBattlefieldAndReturn(player1, new GenePollinator());
        pollinator.setSummoningSick(true);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(pollinator.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent cannot pay the additional tap cost")
    void cannotUseOpponentsPermanent() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");

        assertThat(pollinator.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped permanent cannot pay the additional tap cost")
    void cannotUseTappedPermanent() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");

        assertThat(pollinator.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
