package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightMangler.class, GrizzlyBears.class, LlanowarElves.class})
class MidnightManglerTest extends BaseCardTest {

    @Test
    @DisplayName("Is not a creature during its controller's turn")
    void notCreatureDuringControllerTurn() {
        Permanent mangler = addMidnightManglerReady(player1);

        harness.forceActivePlayer(player1);

        assertThat(gqs.isArtifact(mangler)).isTrue();
        assertThat(gqs.isCreature(gd, mangler)).isFalse();
    }

    @Test
    @DisplayName("Is a 3/3 artifact creature during an opponent's turn")
    void becomesCreatureDuringOpponentTurn() {
        Permanent mangler = addMidnightManglerReady(player1);

        harness.forceActivePlayer(player2);

        assertThat(gqs.isArtifact(mangler)).isTrue();
        assertThat(gqs.isCreature(gd, mangler)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mangler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Crew 2 animates Midnight Mangler and taps the chosen creature")
    void crewAnimatesMangler() {
        Permanent mangler = addMidnightManglerReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mangler)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(mangler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addMidnightManglerReady(player1);
        addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void stopsBeingCreatureWhenControllerTurnBegins() {
        Permanent mangler = addMidnightManglerReady(player1);
        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, mangler)).isTrue();

        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, mangler)).isFalse();
        assertThat(gqs.isArtifact(mangler)).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent mangler = addMidnightManglerReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, mangler)).isTrue();
    }

    @Test
    void tappedCreatureCannotCrew() {
        Permanent mangler = addMidnightManglerReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(gqs.isCreature(gd, mangler)).isFalse();
    }

    @Test
    void twoOnePowerCreaturesCanCrewTogether() {
        Permanent mangler = addMidnightManglerReady(player1);
        Permanent first = addCreatureReady(player1, new LlanowarElves());
        Permanent second = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, mangler)).isTrue();
    }

    private Permanent addMidnightManglerReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new MidnightMangler());
    }
}
