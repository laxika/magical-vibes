package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.p.PrincessLucrezia;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeafarersQuay.class, PrincessLucrezia.class, Johan.class,
        AdunOakenshield.class, DurkwoodBoars.class})
class SeafarersQuayTest extends BaseCardTest {

    @Test
    @DisplayName("Blue legendary creatures can band with other legendary creatures")
    void blueLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent blueLegendary = addCreatureReady(player1, new PrincessLucrezia());
        Permanent otherLegendary = addCreatureReady(player1, new Johan());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(blueLegendary.getBandId()).isNotNull();
        assertThat(blueLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player1, new PrincessLucrezia());
        addCreatureReady(player1, new DurkwoodBoars());

        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("A non-blue legendary creature does not receive the bands-with-other ability")
    void nonBlueLegendaryCannotFormBand() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player1, new Johan());
        addCreatureReady(player1, new AdunOakenshield());

        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability does not apply to legendary creatures controlled by an opponent")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player2, new PrincessLucrezia());
        addCreatureReady(player2, new Johan());

        beginAttackerDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    private void beginAttackerDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers, List<List<Integer>> bands) {
        beginAttackerDeclaration(player);
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, bands));
    }
}
