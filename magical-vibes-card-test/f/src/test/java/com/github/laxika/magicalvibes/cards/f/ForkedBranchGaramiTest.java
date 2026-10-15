package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BodyOfJukai;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.m.MoonlitStrider;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        ForkedBranchGarami.class,
        KamiOfFalseHope.class,
        TeardropKami.class,
        GoblinCohort.class,
        BodyOfJukai.class,
        TorrentOfStone.class,
        MoonlitStrider.class
})
class ForkedBranchGaramiTest extends BaseCardTest {

    private void killGarami() {
        var garami = harness.addToBattlefieldAndReturn(player1, new ForkedBranchGarami());
        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, garami.getId());
    }

    @Test
    @DisplayName("Two separate soulshift triggers return eligible Spirits")
    void returnsUpToTwoEligibleSpirits() {
        Card firstSpirit = new KamiOfFalseHope();
        Card secondSpirit = new TeardropKami();
        Card nonSpirit = new GoblinCohort();
        Card expensiveSpirit = new BodyOfJukai();
        harness.setGraveyard(player1, List.of(firstSpirit, secondSpirit, nonSpirit, expensiveSpirit));

        killGarami();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstSpirit.getId(), secondSpirit.getId());

        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(firstSpirit.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(secondSpirit.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Teardrop Kami");
        harness.assertInGraveyard(player1, "Kami of False Hope");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of False Hope");
        harness.assertInHand(player1, "Teardrop Kami");
        harness.assertInGraveyard(player1, "Goblin Cohort");
        harness.assertInGraveyard(player1, "Body of Jukai");
    }

    @Test
    @DisplayName("Death trigger includes Spirit cards with mana value exactly four")
    void returnsSpiritAtMaximumManaValue() {
        Card boundarySpirit = new MoonlitStrider();
        harness.setGraveyard(player1, List.of(boundarySpirit));

        killGarami();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(boundarySpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(boundarySpirit.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(boundarySpirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Moonlit Strider");
    }

    @Test
    @DisplayName("Each soulshift return may be declined at resolution")
    void mayReturnNoSpirits() {
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        killGarami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Death trigger has no choice when no eligible Spirit exists")
    void noEligibleSpiritMeansNoChoice() {
        harness.setGraveyard(player1, List.of(new GoblinCohort(), new BodyOfJukai()));

        killGarami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift targets only the controller's graveyard")
    void cannotTargetOpponentsSpirit() {
        Card ownSpirit = new KamiOfFalseHope();
        Card opponentsSpirit = new TeardropKami();
        harness.setGraveyard(player1, List.of(ownSpirit));
        harness.setGraveyard(player2, List.of(opponentsSpirit));

        killGarami();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownSpirit.getId());
    }
}
