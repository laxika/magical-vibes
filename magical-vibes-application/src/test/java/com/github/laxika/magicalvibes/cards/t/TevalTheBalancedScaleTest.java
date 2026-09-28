package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TevalTheBalancedScale.class, Forest.class, GrizzlyBears.class})
class TevalTheBalancedScaleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills three, then may return a land tapped and creates a Zombie Druid")
    void attackingMillsThenReturnsLandAndCreatesToken() {
        Forest land = new Forest();
        List<Card> milledCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, milledCards);
        harness.setGraveyard(player1, List.of(land));
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);

        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Zombie Druid");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(CardSubtype.ZOMBIE, CardSubtype.DRUID);
    }

    @Test
    @DisplayName("Declining the land return still mills but does not create a token")
    void decliningLandReturnStillMills() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(land));
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(findPermanents(player1, "Zombie Druid")).isEmpty();
    }

    private Permanent addReadyTeval() {
        return addCreatureReady(player1, new TevalTheBalancedScale());
    }
}
