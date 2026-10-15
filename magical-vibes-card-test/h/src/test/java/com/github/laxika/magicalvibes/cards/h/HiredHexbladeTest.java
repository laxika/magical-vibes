package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiredHexblade.class, WilyGoblin.class, GrizzlyBears.class, PowerWordKill.class})
class HiredHexbladeTest extends BaseCardTest {

    @Test
    void entersWithoutTreasureManaWithoutDrawingOrLosingLife() {
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new HiredHexblade()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void entersWithTreasureManaDrawsAndLosesLife() {
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new WilyGoblin(), new HiredHexblade()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void doesNotTriggerWhenCastWithoutTreasureMana() {
        harness.setHand(player1, List.of(new HiredHexblade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hired Hexblade");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenPutOntoBattlefieldWithoutCasting() {
        Card libraryCard = new HiredHexblade();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new HiredHexblade());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void treasurePaysBlackRequirementAndTriggerSurvivesSourceDestruction() {
        Card libraryCard = new HiredHexblade();
        harness.setHand(player1, List.of(new WilyGoblin(), new HiredHexblade()));
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        int treasureIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hired Hexblade"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hired Hexblade");
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        harness.assertLife(player2, 20);
    }
}
