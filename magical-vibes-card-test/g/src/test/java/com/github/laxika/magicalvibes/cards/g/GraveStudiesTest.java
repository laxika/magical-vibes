package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TeachersPest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Grave Studies")
@CardUsed({GraveStudies.class, TeachersPest.class, GrizzlyBears.class})
class GraveStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Teacher's Pest, then each player sacrifices a creature")
    void conjuresPestThenEachPlayerSacrifices() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GraveStudies(), "{B}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves with no opposing creatures and leaves a returnable conjured card")
    void emptyOpponentBattlefieldStillSacrificesConjuredCard() {
        harness.castFromHand(player1, new GraveStudies(), "{B}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player1, "Teacher's Pest");
        harness.assertInGraveyard(player1, "Grave Studies");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        int pestIndex = -1;
        for (int i = 0; i < gd.playerGraveyards.get(player1.getId()).size(); i++) {
            if (gd.playerGraveyards.get(player1.getId()).get(i).getName().equals("Teacher's Pest")) {
                pestIndex = i;
                break;
            }
        }
        assertThat(pestIndex).isNotNegative();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateGraveyardAbility(player1, pestIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teacher's Pest");
        harness.assertNotInGraveyard(player1, "Teacher's Pest");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can sacrifice an existing creature and keep the conjured Pest")
    void canKeepConjuredCreature() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new TeachersPest());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new TeachersPest());

        harness.castFromHand(player1, new GraveStudies(), "{B}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Teacher's Pest");
        harness.handleMultiplePermanentsChosen(player1, List.of(existing.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(existing.getId()));
        harness.assertOnBattlefield(player1, "Teacher's Pest");
        harness.assertInGraveyard(player1, "Teacher's Pest");
        harness.assertNotOnBattlefield(player2, "Teacher's Pest");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposing.getCard());
    }
}
