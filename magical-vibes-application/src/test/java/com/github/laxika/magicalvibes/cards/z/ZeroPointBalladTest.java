package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZeroPointBallad.class, GrizzlyBears.class, HillGiant.class})
class ZeroPointBalladTest extends BaseCardTest {

    @Test
    void destroysCreaturesWithToughnessAtMostXAndYouLoseXLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void withXSixReturnsOneDestroyedCreatureFromAnyGraveyardUnderYourControl() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(preexistingCreature));
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 6);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(bears.getCard().getId(), giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(giant.getCard()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void doesNotReturnAcreatureWhenXIsLessThanSix() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }
}
