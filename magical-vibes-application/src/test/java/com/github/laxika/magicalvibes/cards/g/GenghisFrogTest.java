package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MichelangeloImproviser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenghisFrog.class, MichelangeloImproviser.class, GrizzlyBears.class})
class GenghisFrogTest extends BaseCardTest {

    @Test
    void ownEntryCreatesMutagen() {
        harness.setHand(player1, List.of(new GenghisFrog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void anotherMutantEntryCreatesMutagen() {
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.setHand(player1, List.of(new MichelangeloImproviser()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void nonMutantEntryDoesNotCreateMutagen() {
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }
}
