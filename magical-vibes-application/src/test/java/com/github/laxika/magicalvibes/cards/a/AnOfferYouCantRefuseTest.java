package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnOfferYouCantRefuseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and gives its controller two Treasures")
    void countersNoncreatureSpellAndGivesItsControllerTwoTreasures() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        assertThat(findTreasures(player1)).hasSize(2);
        assertThat(findTreasures(player2)).isEmpty();
    }

    @Test
    @DisplayName("Rejects a creature spell as a target")
    void rejectsCreatureSpellAsTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    private List<Permanent> findTreasures(Player player) {
        return findPermanents(player, "Treasure");
    }
}
