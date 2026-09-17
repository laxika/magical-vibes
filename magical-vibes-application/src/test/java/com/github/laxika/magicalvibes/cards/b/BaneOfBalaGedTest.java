package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfBalaGed.class, Forest.class})
class BaneOfBalaGedTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player chooses two permanents to exile when Bane of Bala Ged attacks")
    void defendingPlayerChoosesTwoPermanentsToExile() {
        addCreatureReady(player1, new BaneOfBalaGed());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent thirdForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstForest.getId(), secondForest.getId(), thirdForest.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DefendingPlayerChoosesPermanentsToExile.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(firstForest.getId(), thirdForest.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstForest, thirdForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles all available permanents when the defending player controls fewer than two")
    void exilesAllAvailablePermanentsWhenFewerThanTwoExist() {
        addCreatureReady(player1, new BaneOfBalaGed());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
