package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HireACrew.class, GrizzlyBears.class})
class HireACrewTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Villain token with menace and boosts your creatures")
    void createsTokenAndBoostsOwnCreatures() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        castHireACrew();

        Permanent token = findPermanent(player1, "Villain");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        castHireACrew();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        Permanent token = findPermanent(player1, "Villain");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesDoNotReceiveBoost() {
        castHireACrew();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Villain"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated casts boost earlier tokens again but not later tokens retroactively")
    void repeatedCastsBoostOnlyCreaturesPresentForEachResolution() {
        castHireACrew();
        Permanent firstToken = findPermanent(player1, "Villain");
        castHireACrew();

        List<Permanent> tokens = findPermanents(player1, "Villain");
        assertThat(tokens).hasSize(2);
        Permanent secondToken = tokens.stream()
                .filter(token -> !token.getId().equals(firstToken.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondToken)).isEqualTo(1);
    }

    private void castHireACrew() {
        harness.castFromHand(player1, new HireACrew(), "{2}{R}");
        harness.passBothPriorities();
    }
}
