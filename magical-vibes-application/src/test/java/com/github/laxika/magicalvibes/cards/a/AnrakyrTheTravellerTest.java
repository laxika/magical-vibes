package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnrakyrTheTraveller.class, MindStone.class, GrizzlyBears.class})
class AnrakyrTheTravellerTest extends BaseCardTest {

    @Test
    void attacksOfferArtifactFromHandForLifeEqualToManaValue() {
        harness.setLife(player1, 10);
        MindStone mindStone = new MindStone();
        harness.setHand(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mindStone);
    }

    @Test
    void attacksOfferArtifactFromGraveyardForLifeEqualToManaValue() {
        harness.setLife(player1, 10);
        MindStone mindStone = new MindStone();
        harness.setGraveyard(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(mindStone);
    }

    @Test
    void attacksDoNotOfferNonArtifactCards() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addReadyAnrakyr() {
        addCreatureReady(player1, new AnrakyrTheTraveller());
    }
}
