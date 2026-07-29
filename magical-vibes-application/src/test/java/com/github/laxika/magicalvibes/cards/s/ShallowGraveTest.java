package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShallowGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the topmost creature card of your graveyard with haste")
    void returnsTopmostCreatureWithHaste() {
        Card bottom = new GrizzlyBears();
        Card top = new SerraAngel();
        harness.setGraveyard(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent returned = findOnBattlefield(player1.getId(), "Serra Angel");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Skips non-creature cards above the topmost creature card")
    void skipsNonCreatureCardsAboveIt() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new LightningBolt()));
        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("The returned creature is exiled at the beginning of the next end step")
    void returnedCreatureExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Unlike Unearth, a creature that dies before the end step is not exiled")
    void creatureDyingBeforeEndStepGoesToGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(returned.isExileIfLeavesBattlefield()).isFalse();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing when the graveyard holds no creature card")
    void doesNothingWithoutCreatureCard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shallow Grave");
    }

    private Permanent findOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
