package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdherentsHeirloom.class, GrizzlyBears.class, LlanowarElves.class})
class AdherentsHeirloomTest extends BaseCardTest {

    @Test
    void seeksCreatureOfMostPrevalentCreatureTypeInControllersLibrary() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setLibrary(player2, List.of(
                new LlanowarElves(), new LlanowarElves(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AdherentsHeirloom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
    }

    @Test
    void producesCreatureSpellOnlyMana() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new AdherentsHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(heirloom.isTapped()).isTrue();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }
}
