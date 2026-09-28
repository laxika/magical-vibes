package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NellyBorcaImpulsiveAccuser.class, GrizzlyBears.class, Forest.class})
class NellyBorcaImpulsiveAccuserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking suspects the chosen creature and goads all suspected creatures")
    void attackingSuspectsAndGoadsSuspectedCreatures() {
        Permanent nelly = addCreatureReady(player1, new NellyBorcaImpulsiveAccuser());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent alreadySuspected = addCreatureReady(player2, new GrizzlyBears());
        alreadySuspected.setSuspected(true);

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                nelly.getId(), target.getId(), alreadySuspected.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(gqs.isGoaded(gd, alreadySuspected)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger only offers creature targets")
    void attackTriggerOnlyOffersCreatureTargets() {
        Permanent nelly = addCreatureReady(player1, new NellyBorcaImpulsiveAccuser());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                nelly.getId(), creature.getId(), otherCreature.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId());
    }
}
