package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrotherhoodVertibird;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynthInfiltrator.class, GrizzlyBears.class, SolemnSimulacrum.class, Island.class,
        BrotherhoodVertibird.class})
class SynthInfiltratorTest extends BaseCardTest {

    @Test
    void copyingCreatureAddsArtifactAndSynth() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SynthInfiltrator infiltrator = new SynthInfiltrator();
        harness.setHand(player1, List.of(infiltrator));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent copy = findPermanent(player1, "Grizzly Bears");

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SYNTH);
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void decliningToCopyLeavesNoPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SynthInfiltrator infiltrator = new SynthInfiltrator();
        harness.setHand(player1, List.of(infiltrator));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Synth Infiltrator");
        harness.assertInGraveyard(player1, "Synth Infiltrator");
    }

    @Test
    void noCreaturesToCopyDiesWithoutOfferingAChoice() {
        harness.setHand(player1, List.of(new SynthInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synth Infiltrator");
        harness.assertInGraveyard(player1, "Synth Infiltrator");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyingSolemnSimulacrumTriggersItsEnterAbility() {
        harness.addToBattlefield(player2, new SolemnSimulacrum());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new SynthInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Solemn Simulacrum"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent copy = findPermanent(player1, "Solemn Simulacrum");
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.GOLEM, CardSubtype.SYNTH);
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void improvisePaysGenericManaWithSummoningSickArtifacts() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.setHand(player1, List.of(new SynthInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Solemn Simulacrum")).hasSize(2);
        assertThat(findPermanents(player1, "Solemn Simulacrum"))
                .anySatisfy(copy -> {
                    assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.SYNTH);
                    assertThat(copy.isTapped()).isFalse();
                });
    }

    @Test
    void copyingCrewedVehicleAddsCreatureToItsCopiableTypes() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        harness.addToBattlefield(player1, new SolemnSimulacrum());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        harness.setHand(player1, List.of(new SynthInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vehicle.getId());

        Permanent copy = findPermanents(player1, "Brotherhood Vertibird").stream()
                .filter(permanent -> !permanent.getId().equals(vehicle.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.VEHICLE, CardSubtype.SYNTH);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }
}
