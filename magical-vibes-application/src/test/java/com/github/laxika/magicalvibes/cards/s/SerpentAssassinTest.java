package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentAssassin.class, GrizzlyBears.class, BogImp.class, Swamp.class})
class SerpentAssassinTest extends BaseCardTest {

    /**
     * Casts Serpent Assassin, resolves it onto the battlefield, accepts the "may" ability and
     * chooses the target creature and accepts the ETB triggered ability.
     */
    private void castAndAcceptMay(UUID creatureId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Casting Serpent Assassin puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serpent Assassin");
    }

    @Test
    @DisplayName("Resolving puts Serpent Assassin on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }

    @Test
    @DisplayName("Resolving Serpent Assassin prompts for a nonblack creature target")
    void resolvingPromptsForTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities(); // resolve creature spell -> target choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing target destroys the nonblack creature")
    void acceptingMayDestroysTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndAcceptMay(targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may ability does not destroy the creature")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serpent Assassin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("May prompt does not fire when only black creatures exist")
    void noMayPromptWhenOnlyBlackCreatures() {
        harness.addToBattlefield(player2, new BogImp());
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bog Imp");
    }

    @Test
    @DisplayName("May prompt does not fire when no creatures exist")
    void noMayPromptWhenNoCreatures() {
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }

    @Test
    @DisplayName("Accepting may can destroy a nonblack creature you control")
    void acceptingMayCanDestroyOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        castAndAcceptMay(targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serpent Assassin");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May prompt does not fire when only noncreature permanents exist")
    void noMayPromptWhenOnlyNoncreaturePermanentsExist() {
        harness.addToBattlefield(player2, new Swamp());
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Swamp");
        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }

    @Test
    @DisplayName("ETB ability does nothing if its target leaves before resolution")
    void etbDoesNothingIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SerpentAssassin(), "{3}{B}{B}");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Serpent Assassin");
    }
}
