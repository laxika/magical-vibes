package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FootMystic.class, GrizzlyBears.class})
class FootMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 black Ninja token after your permanent leaves the battlefield")
    void createsNinjaAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castFootMystic();

        List<Permanent> ninjas = findPermanents(player1, "Ninja");
        assertThat(ninjas).hasSize(1);
        assertThat(ninjas.getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(ninjas.getFirst().getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, ninjas.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ninjas.getFirst())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Ninja token when no permanent left the battlefield")
    void doesNotCreateNinjaWithoutPermanentLeaving() {
        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy Disappear")
    void opponentPermanentLeavingDoesNotCreateNinja() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    private void castFootMystic() {
        harness.setHand(player1, List.of(new FootMystic()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
