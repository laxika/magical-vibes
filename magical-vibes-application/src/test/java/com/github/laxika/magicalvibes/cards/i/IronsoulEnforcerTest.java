package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronsoulEnforcer.class, FountainOfYouth.class, GrizzlyBears.class})
class IronsoulEnforcerTest extends BaseCardTest {

    @Test
    void returnsArtifactWhenItAttacksAlone() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());

        declareAttackers(player1, List.of(0));
        chooseArtifactAndResolve(artifact);

        assertThat(findPermanentByCardId(artifact.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void returnsArtifactWhenCommanderAttacksAlone() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackers(player1, List.of(1));
        chooseArtifactAndResolve(artifact);

        assertThat(findPermanentByCardId(artifact.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void doesNotTriggerWhenAttackingWithAnotherCreature() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void doesNotTriggerForNoncommanderAttacker() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    private void chooseArtifactAndResolve(FountainOfYouth artifact) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
