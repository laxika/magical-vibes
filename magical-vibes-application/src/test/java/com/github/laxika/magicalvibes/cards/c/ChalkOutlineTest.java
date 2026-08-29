package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChalkOutline.class, Disentomb.class, GrizzlyBears.class, Recollect.class, Reminisce.class, Shock.class})
class ChalkOutlineTest extends BaseCardTest {

    @Test
    void createsDetectiveAndClueWhenCreatureCardLeavesYourGraveyard() {
        addReadyOutline();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        resolveAllStack();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenNoncreatureCardLeavesYourGraveyard() {
        addReadyOutline();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, shock.getId());
        resolveAllStack();

        assertThat(findPermanents(player1, "Detective")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void createsOnlyOneDetectiveAndClueWhenSeveralCreatureCardsLeaveTogether() {
        addReadyOutline();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllStack();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private void addReadyOutline() {
        harness.addToBattlefield(player1, new ChalkOutline());
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
