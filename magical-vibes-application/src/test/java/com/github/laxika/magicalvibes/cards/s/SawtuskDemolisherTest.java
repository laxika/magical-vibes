package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawtuskDemolisher.class, Forest.class})
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
        Permanent creature = addCreatureReady(player2, new SawtuskDemolisher());
        harness.addToBattlefield(player2, new Forest());

        triggerMutation(demolisher);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void canCastForMutateCostTargetingOwnedNonHuman() {
        Permanent target = addCreatureReady(player1, new SawtuskDemolisher());
        harness.setHand(player1, List.of(new SawtuskDemolisher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Sawtusk Demolisher");
    }

    @Test
    void destroyingOwnPermanentCreatesBeastForSelf() {
        Permanent demolisher = addCreatureReady(player1, new SawtuskDemolisher());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        triggerMutation(demolisher);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Beast");
        harness.assertNotOnBattlefield(player2, "Beast");
    }

    @Test
    void vanishedTargetDoesNotCreateBeast() {
        Permanent demolisher = addCreatureReady(player1, new SawtuskDemolisher());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        triggerMutation(demolisher);
        harness.handlePermanentChosen(player1, forest.getId());
        gd.playerBattlefields.get(player2.getId()).remove(forest);
        gd.playerGraveyards.get(player2.getId()).add(forest.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Beast");
        harness.assertNotOnBattlefield(player2, "Beast");
    }

    private void triggerMutation(Permanent demolisher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, demolisher, List.of(demolisher.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
