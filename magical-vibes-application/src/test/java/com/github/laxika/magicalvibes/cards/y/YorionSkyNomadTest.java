package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YorionSkyNomad.class, GrizzlyBears.class, Forest.class, NullRod.class, LayClaim.class,
        AlmightyBrushwagg.class, Pacifism.class})
class YorionSkyNomadTest extends BaseCardTest {

    @Test
    void choosesOwnAndControlledNonlandPermanentsAndReturnsThemAtNextEndStep() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new NullRod());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownCreature.getId(), ownArtifact.getId());
        assertThat(choice.validIds()).doesNotContain(
                ownLand.getId(), opponentCreature.getId(),
                findPermanent(player1, "Yorion, Sky Nomad").getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId(), ownArtifact.getId()));

        harness.assertNotOnBattlefield(player1, "Null Rod");
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Null Rod");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        harness.assertOnBattlefield(player1, "Null Rod");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenNoPermanentsAreEligible() {
        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @CardUsed({YorionSkyNomad.class, AlmightyBrushwagg.class})
    void mayDeclineExileWhenAnEligiblePermanentExists() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({YorionSkyNomad.class, AlmightyBrushwagg.class})
    void returnsAsANewUntappedPermanentWithoutOldCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");

        advanceToEndStep();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Almighty Brushwagg");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({YorionSkyNomad.class, AlmightyBrushwagg.class})
    void exileDuringAnEndStepReturnsAtTheOpponentsNextEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
    }

    @Test
    @CardUsed({YorionSkyNomad.class, AlmightyBrushwagg.class, Pacifism.class})
    void anExiledAuraChoosesALegalAttachmentWhenItReturns() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Pacifism");

        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent yorion = findPermanent(player1, "Yorion, Sky Nomad");
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));
        harness.assertNotOnBattlefield(player1, "Pacifism");

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, yorion.getId());
        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(yorion.getId());
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
    }
}
