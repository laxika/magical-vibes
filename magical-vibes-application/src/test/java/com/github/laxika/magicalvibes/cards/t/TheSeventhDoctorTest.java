package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSeventhDoctor.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class})
class TheSeventhDoctorTest extends BaseCardTest {

    @Test
    void usesArtifactsYouControlAsTheGuessThreshold() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        addDoctorWithHand(new GrizzlyBears());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "1 or less");

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void correctGuessDoesNotInvestigate() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        addDoctorWithHand(new GrizzlyBears());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 1");

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void decliningTheFreeCastInvestigates() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        addDoctorWithHand(new GrizzlyBears());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "1 or less");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void choosingALandOnAWrongGuessInvestigates() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        addDoctorWithHand(new Forest());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 1");

        harness.assertInHand(player1, "Forest");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private Permanent addDoctorWithHand(Card chosenCard) {
        Permanent doctor = addCreatureReady(player1, new TheSeventhDoctor());
        harness.setHand(player1, List.of(chosenCard));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(doctor)));
        return doctor;
    }
}
