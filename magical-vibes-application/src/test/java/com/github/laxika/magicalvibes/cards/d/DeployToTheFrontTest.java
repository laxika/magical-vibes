package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeployToTheFront.class, GrizzlyBears.class, Shock.class, SolRing.class})
class DeployToTheFrontTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Soldier for each creature on the battlefield")
    void createsSoldiersForAllCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new DeployToTheFront(), "{5}{W}{W}");
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Soldier");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Creates no tokens when there are no creatures")
    void createsNoTokensWithoutCreatures() {
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());

        harness.castFromHand(player1, new DeployToTheFront(), "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        harness.assertInGraveyard(player1, "Deploy to the Front");
    }

    @Test
    @DisplayName("Counts existing creature tokens but ignores noncreature permanents")
    void countsExistingTokensWithoutCountingNewTokensAgain() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());

        harness.castFromHand(player1, new DeployToTheFront(), "{5}{W}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);

        harness.castFromHand(player1, new DeployToTheFront(), "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(6);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Counts creatures at resolution after a creature is killed in response")
    void countsCreaturesAtResolution() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent doomed = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castFromHand(player1, new DeployToTheFront(), "{5}{W}{W}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, doomed.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }
}
