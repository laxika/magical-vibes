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
    @DisplayName("Returns an artifact whose mana value is no greater than the attacking Phyrexians' total power")
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
        assertThat(choice.validCardIds()).containsExactly(eligible.getId(), eligibleCreature.getId());

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
}
