package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrillKillDisciple.class, Forest.class})
class ThrillKillDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Squad discards one card and creates one copy per payment")
    void squadDiscardsAndCreatesCopies() {
        harness.setHand(player1, List.of(new ThrillKillDisciple(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCostsAndDiscards(player1, 0, List.of("{1}"), List.of(1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Thrill-Kill Disciple")).isEqualTo(2);
        assertThat(findPermanents(player1, "Thrill-Kill Disciple"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Dying creates a Junk artifact token")
    void deathCreatesJunk() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new ThrillKillDisciple());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, disciple));
        harness.passBothPriorities();

        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(junk.getCard().getSubtypes()).contains(CardSubtype.JUNK);
    }
}
