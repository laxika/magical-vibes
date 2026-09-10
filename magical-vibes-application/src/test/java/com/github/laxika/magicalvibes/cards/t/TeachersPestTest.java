package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Teacher's Pest")
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

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent returned = battlefield.stream()
                .filter(permanent -> permanent.getCard().getName().equals("Teacher's Pest"))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Teacher's Pest");
    }
}
