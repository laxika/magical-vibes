package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FoundryStreetDenizen;
import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfRavnica.class, OreskosSwiftclaw.class, ChildOfNight.class,
        FoundryStreetDenizen.class, RuneclawBear.class, Forest.class, SliverHivelord.class})
class SoulOfRavnicaTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability draws once for each distinct color among controlled permanents")
    void battlefieldAbilityDrawsForEachDistinctColor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SoulOfRavnica());
        harness.addToBattlefield(player1, new OreskosSwiftclaw());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
    }

    @Test
    @DisplayName("Graveyard ability exiles the source and draws for colors on the battlefield")
    void graveyardAbilityExilesSourceAndDrawsForBattlefieldColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfRavnica()));
        harness.addToBattlefield(player1, new OreskosSwiftclaw());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Soul of Ravnica");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Soul of Ravnica"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    void graveyardAbilityDrawsNothingForColorlessPermanentsAndIgnoresOpponentColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfRavnica()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SliverHivelord());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertNotInGraveyard(player1, "Soul of Ravnica");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Soul of Ravnica"));
    }

    @Test
    void battlefieldAbilityCountsAllColorsOfOneMulticoloredPermanent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SoulOfRavnica());
        harness.addToBattlefield(player1, new SliverHivelord());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
    }

    @Test
    void graveyardAbilityCountsColorsAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfRavnica()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateGraveyardAbility(player1, 0);
        harness.addToBattlefield(player1, new OreskosSwiftclaw());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
