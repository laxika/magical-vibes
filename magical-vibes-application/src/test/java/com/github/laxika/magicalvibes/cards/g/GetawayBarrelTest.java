package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GetawayBarrel.class, Shatter.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class GetawayBarrelTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts a random creature from the top thirteen onto the battlefield")
    void deathTriggerPutsRandomCreatureOntoBattlefield() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            library.add(new Forest());
        }
        library.add(new GrizzlyBears());
        for (int i = 0; i < 6; i++) {
            library.add(new Forest());
        }
        HillGiant belowReveal = new HillGiant();
        library.add(belowReveal);
        setUpAndDestroyBarrel(library);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Getaway Barrel");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(13);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(belowReveal);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 13))
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Death trigger puts all revealed cards on the bottom when no creature is revealed")
    void deathTriggerBottomsRevealedCardsWithoutCreature() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            library.add(new Forest());
        }
        HillGiant belowReveal = new HillGiant();
        library.add(belowReveal);
        setUpAndDestroyBarrel(library);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(14);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(belowReveal);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 14))
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Death trigger reveals the entire library when fewer than thirteen cards remain")
    void deathTriggerHandlesShortLibrary() {
        Forest forest = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        setUpAndDestroyBarrel(List.of(forest, creature));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger with an empty library resolves without putting a creature onto the battlefield")
    void deathTriggerHandlesEmptyLibrary() {
        setUpAndDestroyBarrel(List.of());

        harness.assertInGraveyard(player1, "Getaway Barrel");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger puts exactly one eligible creature onto the battlefield and bottoms the other")
    void deathTriggerSelectsOnlyOneOfMultipleCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Forest forest = new Forest();
        setUpAndDestroyBarrel(List.of(bears, forest, giant));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Card chosen = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        assertThat(chosen).isIn(bears, giant);
        Card unchosen = chosen == bears ? giant : bears;
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, unchosen);
    }
    private void setUpAndDestroyBarrel(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GetawayBarrel()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent barrel = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, barrel.getId());
        harness.passBothPriorities();
    }
}
