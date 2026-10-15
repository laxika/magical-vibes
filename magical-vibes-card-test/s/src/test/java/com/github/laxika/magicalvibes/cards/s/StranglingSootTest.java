package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherflameWall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StranglingSoot.class, Sangrophage.class, AetherflameWall.class, Swamp.class})
class StranglingSootTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with toughness 3 or less")
    void destroysCreatureWithToughnessAtMostThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sangrophage());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sangrophage");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 3")
    void cannotTargetCreatureWithGreaterToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherflameWall());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 3 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");

        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("Flashback destroys the target and exiles Strangling Soot")
    void flashbackDestroysAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sangrophage());
        harness.setGraveyard(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sangrophage");
        harness.assertNotInGraveyard(player1, "Strangling Soot");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Strangling Soot"));
    }

    @Test
    @DisplayName("A target whose toughness increases above three survives resolution")
    void increasedToughnessMakesTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sangrophage());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sangrophage");
        harness.assertInGraveyard(player1, "Strangling Soot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target becomes illegal")
    void flashbackExilesWhenTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sangrophage());
        harness.setGraveyard(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sangrophage");
        harness.assertNotInGraveyard(player1, "Strangling Soot");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Strangling Soot"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own creature and goes to the graveyard when cast normally")
    void destroysOwnCreatureAndGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Sangrophage());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Sangrophage");
        harness.assertInGraveyard(player1, "Sangrophage");
        harness.assertInGraveyard(player1, "Strangling Soot");
    }
}
