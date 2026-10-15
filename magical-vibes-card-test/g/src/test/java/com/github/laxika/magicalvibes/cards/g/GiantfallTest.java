package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Giantfall.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,
        GiantGrowth.class, Unsummon.class})
class GiantfallTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode makes your creature deal its power to an opponent's creature")
    void dealsPowerDamageToOpponentCreature() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first mode cannot target a creature you do not control as the source")
    void sourceMustBeControlled() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID sourceId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second mode destroys a target artifact")
    void destroysArtifact() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 1, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The second mode cannot target a creature")
    void destroyModeRequiresArtifact() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeCannotTargetYourOwnCreatureAsVictim() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageIsOneWayAndDoesNotMakeCreaturesFight() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        var victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of(sourceId, victim.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void usesSourcePowerAtResolution() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new Giantfall(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId));
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void dealsNoDamageWhenEitherTargetLeavesBeforeResolution(boolean removeSource) {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Giantfall(), new Unsummon()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId));
        harness.castAndResolveInstant(player1, 0, removeSource ? sourceId : targetId);
        harness.passBothPriorities();

        if (removeSource) {
            harness.assertInHand(player1, "Hill Giant");
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        } else {
            harness.assertOnBattlefield(player1, "Hill Giant");
            harness.assertInHand(player2, "Grizzly Bears");
        }
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giantfall");
    }

    @Test
    void canDestroyYourOwnArtifact() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 1, artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }
}
