package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulpineHarvester.class, DarksteelRelic.class, MyrRetriever.class,
        SolemnSimulacrum.class, GrizzlyBears.class})
class VulpineHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Targets any artifact and returns it when its mana value is within the total power")
    void returnsEligibleArtifact() {
        Card eligible = new DarksteelRelic();
        Card eligibleCreature = new MyrRetriever();
        Card tooExpensive = new SolemnSimulacrum();
        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, eligibleCreature, tooExpensive, nonArtifact));
        addReadyVulpineHarvester();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId(), eligibleCreature.getId(), tooExpensive.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligibleCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(eligible, tooExpensive, nonArtifact);
    }

    @Test
    @DisplayName("A non-Phyrexian attack does not trigger the Harvester")
    void nonPhyrexianAttackDoesNotTrigger() {
        Card artifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(artifact));
        addReadyVulpineHarvester();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
    }

    private void addReadyVulpineHarvester() {
        addCreatureReady(player1, new VulpineHarvester());
    }

    @Test
    void expensiveArtifactCanBeTargetedButStaysInGraveyard() {
        Card artifact = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(artifact));
        addReadyVulpineHarvester();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Solemn Simulacrum");
        harness.assertNotOnBattlefield(player1, "Solemn Simulacrum");
    }

    @Test
    void stillCountsPhyrexianThatIsNoLongerAttacking() {
        Card artifact = new MyrRetriever();
        harness.setGraveyard(player1, List.of(artifact));
        var attacker = addCreatureReady(player1, new VulpineHarvester());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Myr Retriever");
        harness.assertNotInGraveyard(player1, "Myr Retriever");
    }

    @Test
    void usesPowerAtResolution() {
        Card artifact = new MyrRetriever();
        harness.setGraveyard(player1, List.of(artifact));
        var attacker = addCreatureReady(player1, new VulpineHarvester());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        attacker.setPowerModifier(-2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Myr Retriever");
        harness.assertNotOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    void multiplePhyrexiansProduceOneReturnPerHarvester() {
        Card first = new MyrRetriever();
        Card second = new MyrRetriever();
        harness.setGraveyard(player1, List.of(first, second));
        addReadyVulpineHarvester();
        addReadyVulpineHarvester();

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Myr Retriever")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
