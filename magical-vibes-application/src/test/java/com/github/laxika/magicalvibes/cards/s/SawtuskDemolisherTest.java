package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawtuskDemolisher.class, Forest.class, GrizzlyBears.class})
class SawtuskDemolisherTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating destroys a target noncreature permanent and gives its controller a 3/3 Beast")
    void mutatingDestroysNoncreatureAndCreatesBeastForItsController() {
        Permanent demolisher = addCreatureReady(player1, new SawtuskDemolisher());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        triggerMutation(demolisher);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Beast")
                        && permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getColor() == CardColor.GREEN
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BEAST));
    }

    @Test
    @DisplayName("The mutation trigger cannot target a creature")
    void cannotTargetCreature() {
        Permanent demolisher = addCreatureReady(player1, new SawtuskDemolisher());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        triggerMutation(demolisher);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void triggerMutation(Permanent demolisher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, demolisher, List.of(demolisher.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
