package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfWeaving.class, GrizzlyBears.class, UrzasBauble.class, Plains.class})
class SeasonOfWeavingTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose no modes")
    void canChooseNoModes() {
        cast(0);

        assertThat(tokensOf(player1)).isEmpty();
    }

    @Test
    @DisplayName("Can choose the draw mode five times")
    void drawsFiveCards() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));

        cast(5);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The copy mode chooses a controlled artifact or creature during resolution")
    void choosesControlledArtifactOrCreatureAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new UrzasBauble());

        cast(6);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), bauble.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ChooseControlledArtifactOrCreatureToCopy.class);

        harness.handlePermanentChosen(player1, bauble.getId());

        assertThat(tokensOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The same copy mode can be chosen twice")
    void copiesModeTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(10);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(tokensOf(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Returns each nonland nontoken permanent on every battlefield")
    void returnsEachNonlandNontokenPermanent() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(6);
        cast(12);

        assertThat(gd.playerHands.get(player1.getId())).contains(ownBears.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentBears.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand);
        assertThat(tokensOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The copy mode does nothing when the controller controls no artifact or creature")
    void noControlledArtifactOrCreatureDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(6);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(tokensOf(player1)).isEmpty();
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new SeasonOfWeaving()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, modeIndex);
        harness.passBothPriorities();
    }

    private List<Permanent> tokensOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
