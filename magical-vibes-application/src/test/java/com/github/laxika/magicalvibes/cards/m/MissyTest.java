package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdricMathematicalGenius;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.t.TheDoctorsTomb;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesMissyVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Missy.class, AdricMathematicalGenius.class, ClockworkDroid.class,
        Ponder.class, TheDoctorsTomb.class})
class MissyTest extends BaseCardTest {

    @Test
    void returnsAnotherNonartifactCreatureAsTappedFaceDownCybermanUnderYourControl() {
        harness.addToBattlefield(player1, new Missy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AdricMathematicalGenius());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getFaceDownPower()).isEqualTo(2);
        assertThat(returned.getFaceDownToughness()).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, returned))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.CYBERMAN)).isTrue();
    }

    @Test
    void artifactCreatureDeathDoesNotReturn() {
        harness.addToBattlefield(player1, new Missy());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        artifact.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Clockwork Droid");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(artifact.getCard().getId()));
    }

    @Test
    void opponentChoosesArtifactDamageOrControllerDraws() {
        harness.addToBattlefield(player1, new Missy());
        harness.addToBattlefield(player1, new ClockworkDroid());
        var libraryBears = new AdricMathematicalGenius();
        harness.setLibrary(player1, List.of(libraryBears));

        resolveEndStep();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION,
                EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION);

        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(libraryBears.getId()));
    }

    @Test
    void opponentCanChooseControllerDrawsAndChaos() {
        harness.addToBattlefield(player1, new Missy());
        harness.setLibrary(player1, List.of(new AdricMathematicalGenius()));

        resolveEndStep();
        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION);

        harness.assertInHand(player1, "Adric, Mathematical Genius");
    }

    @Test
    void returnedCybermanDoesNotReturnAgainWhenItDies() {
        harness.addToBattlefield(player1, new Missy());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdricMathematicalGenius());
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent cyberman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()))
                .findFirst().orElseThrow();
        cyberman.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Adric, Mathematical Genius");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
    }

    @Test
    void faceDownArtifactCardReturnsBecauseItWasANonartifactCreature() {
        harness.addToBattlefield(player1, new Missy());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(creature.getCard().getId());
            assertThat(permanent.isFaceDown()).isTrue();
            assertThat(permanent.isTapped()).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.CYBERMAN)).isTrue();
        });
    }

    @Test
    void faceDownSorceryReturnsAsCyberman() {
        harness.addToBattlefield(player1, new Missy());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Ponder());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(creature.getCard().getId());
            assertThat(permanent.isFaceDown()).isTrue();
            assertThat(permanent.isTapped()).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.CYBERMAN)).isTrue();
        });
    }

    @Test
    void missyDoesNotReturnHerselfButSeesOtherSimultaneousDeaths() {
        Permanent missy = harness.addToBattlefieldAndReturn(player1, new Missy());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdricMathematicalGenius());
        missy.setMarkedDamage(5);
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Missy");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(creature.getCard().getId());
            assertThat(permanent.isFaceDown()).isTrue();
        });
    }

    @Test
    void damageChoiceWithNoArtifactCreaturesDoesNothing() {
        harness.addToBattlefield(player1, new Missy());
        harness.setLibrary(player1, List.of(new Ponder()));
        resolveEndStep();
        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 20);
        harness.assertNotInHand(player1, "Ponder");
    }

    @Test
    void everyArtifactCreatureDealsOneDamageIncludingTappedCybermen() {
        harness.addToBattlefield(player1, new Missy());
        harness.addToBattlefield(player1, new ClockworkDroid());
        Permanent cyberman = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        cyberman.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE), Set.of(CardSubtype.CYBERMAN));
        cyberman.tap();
        resolveEndStep();
        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 18);
    }

    @Test
    void drawChoiceAlsoTriggersTheCurrentPlanesChaosAbility() {
        harness.addToBattlefield(player1, new Missy());
        harness.setLibrary(player1, List.of(new Ponder()));
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheDoctorsTomb(), 1));
        resolveEndStep();
        harness.handleListChoice(player2, EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION);

        harness.assertInHand(player1, "Ponder");
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard()).isInstanceOf(TheDoctorsTomb.class);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
