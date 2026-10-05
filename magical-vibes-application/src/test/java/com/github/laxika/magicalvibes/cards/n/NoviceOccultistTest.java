package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EatenAlive;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoviceOccultist.class, Forest.class, Shock.class, InfernalGrasp.class, EatenAlive.class})
class NoviceOccultistTest extends BaseCardTest {

    @Test
    void drawsCardAndLosesLifeWhenItDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NoviceOccultist());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Novice Occultist");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertInGraveyard(player1, "Novice Occultist");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void deathTriggerBenefitsTheDyingCreaturesController() {
        Forest drawnCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Novice Occultist"));

        harness.assertInGraveyard(player2, "Novice Occultist");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void exileDoesNotTriggerTheDeathAbility() {
        Forest libraryCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player2, "Novice Occultist"), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Novice Occultist");
        harness.assertNotInGraveyard(player2, "Novice Occultist");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof NoviceOccultist);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    void sacrificingOccultistAsACostTriggersItsAbility() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NoviceOccultist());
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player2, "Novice Occultist"),
                harness.getPermanentId(player1, "Novice Occultist"));

        harness.assertInGraveyard(player1, "Novice Occultist");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertOnBattlefield(player2, "Novice Occultist");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Novice Occultist");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
    }
}
