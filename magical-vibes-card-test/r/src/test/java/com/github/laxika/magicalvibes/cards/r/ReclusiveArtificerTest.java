package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReclusiveArtificer.class, Ornithopter.class, GrizzlyBears.class, Disperse.class})
class ReclusiveArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of artifacts controlled")
    void etbDealsDamageEqualToArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With one artifact the damage is only 1 and the creature survives")
    void damageScalesWithArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may deals no damage")
    void decliningMayDealsNoDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("No artifacts means zero damage, even when the opponent controls artifacts")
    void opposingArtifactsDoNotCount() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Artifacts entering after the trigger is stacked count on resolution")
    void artifactCountIncreasesBeforeResolution() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The optional damage can target a creature controlled by the Artificer's controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The entering Artificer can target itself")
    void canTargetItself() {
        harness.addToBattlefield(player1, new Ornithopter());
        castArtificer();
        UUID artificerId = harness.getPermanentId(player1, "Reclusive Artificer");
        harness.handlePermanentChosen(player1, artificerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent artificer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(artificerId))
                .findFirst().orElseThrow();
        assertThat(artificer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifacts leaving before resolution reduce the damage")
    void artifactCountDecreasesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());

        bounceInResponse(artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ornithopter");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The damage trigger still resolves after the Artificer leaves")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());

        bounceInResponse(harness.getPermanentId(player1, "Reclusive Artificer"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Reclusive Artificer");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A trigger whose creature target leaves does not select a replacement target")
    void removedTargetDoesNotCauseRetargeting() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());

        bounceInResponse(bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Reclusive Artificer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void bounceInResponse(UUID targetId) {
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castAndAcceptMay(UUID targetId) {
        castArtificer();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // resolve the ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castArtificer() {
        harness.castFromHand(player1, new ReclusiveArtificer(), "{2}{U}{R}");
        harness.passBothPriorities(); // resolve the creature spell
    }
}
