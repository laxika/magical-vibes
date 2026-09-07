package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrespasserIlVec.class, GrizzlyBears.class})
class TrespasserIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Trespasser il-Vec requires discarding a card")
    void activationRequiresDiscardingACard() {
        addTrespasserReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("Discarding a card gives Trespasser il-Vec shadow until end of turn")
    void resolvingAbilityGrantsShadow() {
        Permanent trespasser = addTrespasserReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        Permanent trespasser = addTrespasserReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addTrespasserReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    private Permanent addTrespasserReady(Player player) {
        Permanent permanent = new Permanent(new TrespasserIlVec());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
