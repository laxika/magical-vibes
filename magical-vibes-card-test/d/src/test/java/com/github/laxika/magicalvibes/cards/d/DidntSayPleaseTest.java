package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DidntSayPlease.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class, SpellbreakerBehemoth.class})
class DidntSayPleaseTest extends BaseCardTest {

    private void prepareCaster() {
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Counters the target spell and mills its controller three cards")
    void countersAndMillsThree() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    @DisplayName("Still mills three if the targeted spell can't be countered")
    void millsEvenIfUncounterable() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    @DisplayName("The countered spell enters the graveyard before the milled cards")
    void countersBeforeMillingWithShortLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(first, second));
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(bears, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isSameAs(bears);
        harness.assertInGraveyard(player2, "Didn't Say Please");
    }

    @Test
    @DisplayName("Does not mill again when its target has already been countered")
    void missingTargetDoesNotMill() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new DidntSayPlease(), new DidntSayPlease()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
