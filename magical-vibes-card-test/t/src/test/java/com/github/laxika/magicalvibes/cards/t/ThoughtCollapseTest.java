package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtCollapse.class, Forest.class, GrizzlyBears.class, AvatarOfMight.class,
        SpellbreakerBehemoth.class, AxebaneBeast.class})
class ThoughtCollapseTest extends BaseCardTest {

    private void prepareCaster() {
        harness.setHand(player2, List.of(new ThoughtCollapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Counters the target spell and mills its controller three cards")
    void countersAndMillsThree() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    @DisplayName("Still mills three if the targeted spell cannot be countered")
    void millsEvenIfUncounterable() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castFromHand(player1, avatar, "{6}{G}{G}");
        harness.passPriority(player1);
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    void counteredSpellEntersGraveyardBeforeMilledCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AxebaneBeast beast = new AxebaneBeast();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, new Forest()));
        prepareCaster();

        harness.castFromHand(player1, beast, "{3}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beast.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(beast, first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isSameAs(beast);
    }

    @Test
    void millsAllRemainingCardsFromShortLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AxebaneBeast beast = new AxebaneBeast();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        prepareCaster();

        harness.castFromHand(player1, beast, "{3}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beast.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(beast, first, second);
        harness.assertNotOnBattlefield(player1, "Axebane Beast");
    }

    @Test
    void doesNotMillWhenTargetLeavesStackBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AxebaneBeast beast = new AxebaneBeast();
        List<Forest> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player2, List.of(new ThoughtCollapse(), new ThoughtCollapse()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, beast, "{3}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, beast.getId());
        harness.castInstant(player2, 0, beast.getId());

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        harness.assertInGraveyard(player1, "Axebane Beast");
        assertThat(gd.stack).isEmpty();
    }
}
