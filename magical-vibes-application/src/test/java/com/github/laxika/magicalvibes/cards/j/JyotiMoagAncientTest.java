package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JyotiMoagAncient.class, GrizzlyBears.class})
class JyotiMoagAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Forest Dryad land creature for each commander cast")
    void createsForestDryadsForCommanderCasts() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();

        List<Permanent> dryads = findPermanents(player1, "Forest Dryad");
        assertThat(dryads).hasSize(2);
        assertThat(dryads).allSatisfy(dryad -> {
            assertThat(dryad.getCard().hasType(CardType.LAND)).isTrue();
            assertThat(dryad.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(dryad.getCard().getSubtypes())
                    .contains(CardSubtype.FOREST, CardSubtype.DRYAD);
        });
    }

    @Test
    @DisplayName("Gives land creatures +2/+2 at the beginning of combat")
    void boostsLandCreaturesByJyotisPower() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        Permanent nonlandCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent dryad = findPermanent(player1, "Forest Dryad");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonlandCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonlandCreature)).isEqualTo(2);
    }
}
