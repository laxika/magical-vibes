package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
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

@CardUsed({Countermand.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class, SpellbreakerBehemoth.class})
class CountermandTest extends BaseCardTest {

    private void prepareCaster() {
        harness.setHand(player2, List.of(new Countermand()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Counters the target spell and mills its controller four cards")
    void countersAndMillsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
    }

    @Test
    @DisplayName("Still mills four if the targeted spell can't be countered")
    void millsEvenIfUncounterable() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castFromHand(player1, avatar, "{6}{G}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, avatar.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
    }

    @Test
    @DisplayName("Puts the countered spell in the graveyard before the milled cards")
    void countersBeforeMilling() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isSameAs(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters the spell even when its controller has fewer than four library cards")
    void millsAllOfShortLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        prepareCaster();

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not mill when another counterspell has already removed its target")
    void doesNotMillWithIllegalTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Countermand(), new Countermand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
}
