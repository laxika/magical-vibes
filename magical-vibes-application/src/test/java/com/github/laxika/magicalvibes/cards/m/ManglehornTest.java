package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EdificeOfAuthority;
import com.github.laxika.magicalvibes.cards.f.FinalReward;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Manglehorn.class, Ornithopter.class, GrizzlyBears.class, EdificeOfAuthority.class, FinalReward.class})
class ManglehornTest extends BaseCardTest {

    /**
     * Casts Manglehorn, resolves it onto the battlefield, then accepts the may ability and
     * chooses the target artifact so the ETB destruction resolves.
     */
    private void castAndAcceptMay(UUID artifactId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Accepting may and choosing artifact destroys it")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        castAndAcceptMay(artifactId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Declining may ability does not destroy the artifact")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Ornithopter"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("May prompt does not fire when no artifact is on the battlefield")
    void noMayPromptWhenNoArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Manglehorn");
    }

    @Test
    @DisplayName("Opponent's artifacts enter tapped")
    void opponentsArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new Manglehorn());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's own artifacts do NOT enter tapped")
    void controllersArtifactsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new Manglehorn());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ETB ability can destroy its controller's own artifact")
    void etbCanDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new EdificeOfAuthority());
        castAndAcceptMay(harness.getPermanentId(player1, "Edifice of Authority"));

        harness.assertNotOnBattlefield(player1, "Edifice of Authority");
        harness.assertInGraveyard(player1, "Edifice of Authority");
        harness.assertOnBattlefield(player1, "Manglehorn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability destroys noncreature artifacts")
    void etbDestroysNoncreatureArtifact() {
        harness.addToBattlefield(player2, new EdificeOfAuthority());
        castAndAcceptMay(harness.getPermanentId(player2, "Edifice of Authority"));

        harness.assertNotOnBattlefield(player2, "Edifice of Authority");
        harness.assertInGraveyard(player2, "Edifice of Authority");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's noncreature artifacts enter tapped even without being cast")
    void noncreatureArtifactEnteringWithoutBeingCastIsTapped() {
        harness.addToBattlefield(player1, new Manglehorn());

        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new EdificeOfAuthority());

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Edifice of Authority");
    }

    @Test
    @DisplayName("Opponent's nonartifact creatures enter untapped")
    void nonartifactCreatureEntersUntapped() {
        harness.addToBattlefield(player1, new Manglehorn());

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Manglehorn does not tap artifacts already on the battlefield")
    void existingArtifactRemainsUntappedWhenManglehornEnters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The destruction trigger still resolves after Manglehorn leaves")
    void etbResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player2, new EdificeOfAuthority());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Edifice of Authority"));

        harness.setHand(player2, List.of(new FinalReward()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Manglehorn"));
        harness.assertNotOnBattlefield(player1, "Manglehorn");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Edifice of Authority");
        harness.assertInGraveyard(player2, "Edifice of Authority");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A removed artifact target causes the trigger to fail without a may prompt")
    void removedTargetPreventsMayPrompt() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Manglehorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);

        harness.setHand(player2, List.of(new FinalReward()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Manglehorn");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
