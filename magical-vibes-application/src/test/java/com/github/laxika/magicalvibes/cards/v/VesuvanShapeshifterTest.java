package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanShapeshifter.class, GrizzlyBears.class, FathomSeer.class, Island.class})
class VesuvanShapeshifterTest extends BaseCardTest {

    @Test
    void entersAsCopyAndCanTurnFaceDownOnUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VesuvanShapeshifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent shapeshifter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Vesuvan Shapeshifter"))
                .findFirst().orElseThrow();
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, shapeshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shapeshifter)).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(shapeshifter.isFaceDown()).isTrue();
    }

    @Test
    void copiesCreatureAsItIsTurnedFaceUpAndKeepsThatCreaturesTrigger() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent fathomSeer = addCreatureReady(player2, new FathomSeer());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new VesuvanShapeshifter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent shapeshifter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Vesuvan Shapeshifter"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shapeshifter));
        harness.handlePermanentChosen(player1, fathomSeer.getId());
        harness.passBothPriorities();

        assertThat(shapeshifter.isFaceDown()).isFalse();
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Fathom Seer");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstIsland, secondIsland);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
    }
}
