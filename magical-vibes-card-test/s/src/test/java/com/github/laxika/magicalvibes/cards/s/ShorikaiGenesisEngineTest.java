package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.w.Weatherlight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShorikaiGenesisEngine.class, Forest.class, GrizzlyBears.class, Weatherlight.class, TrainedArynx.class})
class ShorikaiGenesisEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and creates an enhanced Pilot")
    void drawsDiscardsAndCreatesPilot() {
        addShorikai();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPilot()).isNotNull();
    }

    @Test
    @DisplayName("The Pilot contributes two additional power when crewing")
    void pilotEnhancesCrewPower() {
        addShorikai();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent pilot = findPilot();
        pilot.setSummoningSick(false);
        Permanent weatherlight = addCreatureReady(player1, new Weatherlight());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weatherlight), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew 8 animates Shorikai with eight total creature power")
    void crewEightAnimatesShorikai() {
        addShorikai();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent shorikai = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, shorikai)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).subList(1, 5))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("A newly entered uncrewed Shorikai can activate its tap ability")
    void newlyEnteredArtifactCanDraw() {
        Permanent shorikai = harness.addToBattlefieldAndReturn(player1, new ShorikaiGenesisEngine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shorikai.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Pilot")).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Pilot")).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Pilot")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPilot())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPilot())).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly created Pilot can crew despite summoning sickness")
    void newlyCreatedPilotCanCrew() {
        createPilot();
        Permanent pilot = findPilot();
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new Weatherlight());

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Pilot's crew bonus does not apply to saddle costs")
    void pilotCannotPaySaddleTwoAlone() {
        createPilot();
        Permanent arynx = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(findPilot().isTapped()).isFalse();
        assertThat(arynx.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Crew 8 rejects six total creature power without tapping creatures")
    void cannotCrewWithInsufficientPower() {
        addShorikai();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Crew animation expires at the end of the turn")
    void crewAnimationExpires() {
        addShorikai();
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        Permanent shorikai = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, shorikai)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, shorikai)).isFalse();
    }

    private void createPilot() {
        addShorikai();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void addShorikai() {
        addCreatureReady(player1, new ShorikaiGenesisEngine());
    }

    private Permanent findPilot() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .findFirst()
                .orElseThrow();
    }
}
