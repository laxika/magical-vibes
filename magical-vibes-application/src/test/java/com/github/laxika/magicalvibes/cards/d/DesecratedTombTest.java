package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesecratedTombTest extends BaseCardTest {

    @Test
    void createsBatWhenCreatureCardLeavesYourGraveyard() {
        addReadyTomb(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenNoncreatureCardLeavesYourGraveyard() {
        addReadyTomb(player1);
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsOnlyOneBatWhenSeveralCreatureCardsLeaveTogether() {
        addReadyTomb(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    private void addReadyTomb(Player player) {
        harness.addToBattlefield(player, new DesecratedTomb());
    }
}
