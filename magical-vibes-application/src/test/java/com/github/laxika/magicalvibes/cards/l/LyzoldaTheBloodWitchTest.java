package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LyzoldaTheBloodWitch.class, DregscapeZombie.class, GrizzlyBears.class, RagingGoblin.class})
class LyzoldaTheBloodWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when the sacrificed creature was red")
    void dealsDamageForRedCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new RagingGoblin());
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lyzolda, the Blood Witch");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card when the sacrificed creature was black")
    void drawsForBlackCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new DregscapeZombie());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Does nothing extra when the sacrificed creature was neither red nor black")
    void doesNothingForGreenCreature() {
        addLyzolda();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addLyzolda() {
        return addCreatureReady(player1, new LyzoldaTheBloodWitch());
    }
}
