package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElephantResurgence.class, DivingGriffin.class, Abolish.class})
class ElephantResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Each player creates an Elephant whose P/T count creature cards in that player's graveyard")
    void eachPlayerCreatesElephantWithOwnGraveyardCount() {
        harness.setGraveyard(player1, List.of(new DivingGriffin(), new Abolish()));
        harness.setGraveyard(player2, List.of(new DivingGriffin(), new DivingGriffin(), new Abolish()));

        castElephantResurgence();

        Permanent playerOneElephant = findPermanent(player1, "Elephant");
        Permanent playerTwoElephant = findPermanent(player2, "Elephant");
        assertThat(gqs.getEffectivePower(gd, playerOneElephant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, playerOneElephant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, playerTwoElephant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, playerTwoElephant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elephant power and toughness update as creature cards enter its controller's graveyard")
    void elephantUpdatesWithGraveyard() {
        harness.setGraveyard(player1, List.of(new DivingGriffin()));
        harness.setGraveyard(player2, List.of(new DivingGriffin()));

        castElephantResurgence();

        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new DivingGriffin(), new DivingGriffin()));

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elephants with no creature cards in their controller's graveyard die as 0/0s")
    void elephantsWithNoCreatureCardsDieImmediately() {
        harness.setGraveyard(player1, List.of(new Abolish()));
        harness.setGraveyard(player2, List.of(new Abolish()));

        castElephantResurgence();

        assertThat(findPermanents(player1, "Elephant")).isEmpty();
        assertThat(findPermanents(player2, "Elephant")).isEmpty();
    }

    private void castElephantResurgence() {
        harness.castFromHand(player1, new ElephantResurgence(), "{1}{G}");
        harness.passBothPriorities();
    }
}
