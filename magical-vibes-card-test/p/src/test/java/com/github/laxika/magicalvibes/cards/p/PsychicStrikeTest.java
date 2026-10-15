package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PsychicStrike.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        SpellbreakerBehemoth.class})
class PsychicStrikeTest extends BaseCardTest {

    private void prepareCaster() {
        harness.setHand(player2, List.of(new PsychicStrike()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Counters the target spell and mills its controller two cards")
    void countersAndMillsTwo() {
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
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Still mills two if the targeted spell can't be countered")
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
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("The countered spell enters the graveyard before the milled cards")
    void countersBeforeMilling() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(forest));
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters a spell even when its controller has an empty library")
    void countersWithEmptyLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of());
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not mill when its only target has already left the stack")
    void doesNotMillWithIllegalTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new PsychicStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        prepareCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Psychic Strike");
    }
}
