package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeastriderVanguard;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyboxFerry.class, BeastriderVanguard.class})
class SkyboxFerryTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 2 animates Skybox Ferry until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent ferry = addCreatureReady(player1, new SkyboxFerry());
        Permanent crew = addCreatureReady(player1, new BeastriderVanguard());

        activate(ferry);

        assertThat(gqs.isCreature(gd, ferry)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ferry)).isFalse();
    }

    @Test
    @DisplayName("Cycling Skybox Ferry discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SkyboxFerry()));
        harness.setLibrary(player1, List.of(new BeastriderVanguard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skybox Ferry");
        harness.assertInHand(player1, "Beastrider Vanguard");
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay crew, and animation waits for resolution")
    void summoningSickCreatureCanCrew() {
        Permanent ferry = harness.addToBattlefieldAndReturn(player1, new SkyboxFerry());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ferry)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ferry)).isTrue();
        assertThat(ferry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures cannot pay crew")
    void tappedCreatureCannotCrew() {
        harness.addToBattlefield(player1, new SkyboxFerry());
        Permanent crew = addCreatureReady(player1, new BeastriderVanguard());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creatures cannot pay crew")
    void opponentsCreatureCannotCrew() {
        harness.addToBattlefield(player1, new SkyboxFerry());
        Permanent opponentCreature = addCreatureReady(player2, new BeastriderVanguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A crewed Skybox Ferry cannot be blocked by a creature without flying or reach")
    void crewedFerryHasFlyingInCombat() {
        Permanent ferry = addCreatureReady(player1, new SkyboxFerry());
        addCreatureReady(player1, new BeastriderVanguard());
        addCreatureReady(player2, new BeastriderVanguard());
        activate(ferry);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling pays mana and discards immediately but draws only on resolution")
    void cyclingPaysCostsBeforeDrawing() {
        harness.setHand(player1, List.of(new SkyboxFerry()));
        harness.setLibrary(player1, List.of(new BeastriderVanguard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Skybox Ferry");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Beastrider Vanguard");
    }

    @Test
    @DisplayName("Cycling cannot be activated with insufficient mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new SkyboxFerry()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Skybox Ferry");
        harness.assertNotInGraveyard(player1, "Skybox Ferry");
        assertThat(gd.stack).isEmpty();
    }

    private void activate(Permanent ferry) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ferry);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
