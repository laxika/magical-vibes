package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrbitalPlunge.class, GrizzlyBears.class, ScaledWurm.class, FountainOfYouth.class, Forest.class})
class OrbitalPlungeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Lander token when it deals excess damage")
    void createsLanderAfterExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOrbitalPlunge(target);

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Does not create a Lander token when it deals no excess damage")
    void doesNotCreateLanderWithoutExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScaledWurm());
        castOrbitalPlunge(target);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Scaled Wurm"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new OrbitalPlunge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void previouslyMarkedDamageCountsTowardExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScaledWurm());
        target.setMarkedDamage(1);

        castOrbitalPlunge(target);

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        harness.assertInGraveyard(player2, "Scaled Wurm");
    }

    @Test
    void fullyPreventedDamageDoesNotCreateLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(6);

        castOrbitalPlunge(target);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void partiallyPreventedExactlyLethalDamageDoesNotCreateLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(4);

        castOrbitalPlunge(target);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void landerCanImmediatelyBeSacrificedToFindTappedBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOrbitalPlunge(target);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), forest));
        Permanent lander = findPermanents(player1, "Lander").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lander);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
        assertThat(gd.playerLibraries.get(player1.getId())).doesNotContain(forest);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void landerSearchCanFailToFind() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOrbitalPlunge(target);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerLibraries.get(player1.getId())).containsExactly(forest);
    }


    private void castOrbitalPlunge(Permanent target) {
        harness.setHand(player1, List.of(new OrbitalPlunge()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
