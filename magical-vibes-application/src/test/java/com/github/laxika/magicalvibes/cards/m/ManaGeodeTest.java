package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaGeode.class})
class ManaGeodeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield triggers scry 1")
    void enteringTriggersScryOne() {
        harness.setHand(player1, List.of(new ManaGeode()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent geode = harness.addToBattlefieldAndReturn(player1, new ManaGeode());
        geode.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(geode.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void newlyEnteredArtifactCanProduceEveryColor(ManaColor color) {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ManaGeode()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void scryCanKeepOrBottomTheTopCard(boolean bottom) {
        ManaGeode top = new ManaGeode();
        ManaGeode next = new ManaGeode();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new ManaGeode()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                bottom ? List.of() : List.of(0), bottom ? List.of(0) : List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(bottom ? List.of(next, top) : List.of(top, next));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArtifactCannotProduceManaAgain() {
        Permanent geode = harness.addToBattlefieldAndReturn(player1, new ManaGeode());
        geode.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
