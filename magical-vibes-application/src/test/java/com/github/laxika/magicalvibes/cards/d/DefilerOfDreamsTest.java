package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Defiler of Dreams")
@CardUsed({DefilerOfDreams.class, CloudfinRaptor.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class DefilerOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a blue permanent spell by {U} and draws a card")
    void paysLifeForBluePermanentSpell() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.setLife(player1, 20);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, null, null, null, List.of(), List.of(), false,
                null, null, List.of(), List.of(), null, null, false, true, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("paying the reduced blue mana cost leaves life unchanged and draws a card")
    void paysReducedManaForBluePermanentSpell() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("non-blue permanents and blue nonpermanents do not trigger or receive the reduction")
    void ignoresNonMatchingSpells() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);

        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, gd.playerBattlefields.get(player2.getId()).getFirst().getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }
}
