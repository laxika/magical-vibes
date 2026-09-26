package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GwaihirGreatestOfTheEagles.class, GrizzlyBears.class})
class GwaihirGreatestOfTheEaglesTest extends BaseCardTest {

    @Test
    @DisplayName("Gwaihir's attack trigger targets an attacking creature")
    void attackTriggerTargetsAttackingCreature() {
        Permanent gwaihir = addCreatureReady(player1, new GwaihirGreatestOfTheEagles());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent groundCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(gwaihir.getId(), bears.getId())
                .doesNotContain(groundCreature.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gwaihir creates a Bird token after gaining at least 3 life")
    void createsBirdTokenAfterGainingThreeLife() {
        addCreatureReady(player1, new GwaihirGreatestOfTheEagles());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getCard().getPower()).isEqualTo(3);
        assertThat(bird.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The created Bird's attack trigger grants flying")
    void createdBirdHasAttackTrigger() {
        addCreatureReady(player1, new GwaihirGreatestOfTheEagles());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        bird.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(bird.getId(), bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gwaihir does not create a Bird token below the life threshold")
    void doesNotCreateBirdTokenBelowThreshold() {
        addCreatureReady(player1, new GwaihirGreatestOfTheEagles());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
