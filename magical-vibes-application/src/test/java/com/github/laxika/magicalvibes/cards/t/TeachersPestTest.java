package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Teacher's Pest")
@CardUsed({TeachersPest.class})
class TeachersPestTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking causes you to gain 1 life")
    void attackingGainsOneLife() {
        addCreatureReady(player1, new TeachersPest());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Graveyard ability returns it to the battlefield tapped")
    void graveyardAbilityReturnsItTapped() {
        TeachersPest pest = new TeachersPest();
        harness.setGraveyard(player1, List.of(pest));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Teacher's Pest");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Teacher's Pest");
    }

    @Test
    void onlyTheActivatingCopyReturns() {
        TeachersPest first = new TeachersPest();
        TeachersPest second = new TeachersPest();
        TeachersPest opposing = new TeachersPest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposing));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Teacher's Pest").getCard()).isSameAs(second);
        assertThat(findPermanent(player1, "Teacher's Pest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
        harness.assertNotOnBattlefield(player2, "Teacher's Pest");
    }

    @Test
    void cannotReturnWithoutGreenMana() {
        harness.setGraveyard(player1, List.of(new TeachersPest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player1, "Teacher's Pest");
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new TeachersPest());
        addCreatureReady(player2, new TeachersPest());
        addCreatureReady(player2, new TeachersPest());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockersAndAttackStillGainsLife() {
        addCreatureReady(player1, new TeachersPest());
        Permanent first = addCreatureReady(player2, new TeachersPest());
        Permanent second = addCreatureReady(player2, new TeachersPest());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }
}
