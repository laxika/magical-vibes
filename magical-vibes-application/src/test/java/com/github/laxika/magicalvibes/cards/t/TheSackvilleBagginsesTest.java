package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSackvilleBagginses.class, GrizzlyBears.class, MindStone.class})
class TheSackvilleBagginsesTest extends BaseCardTest {

    @Test
    void maySacrificeAnotherCreatureDrawsAndCreatesTreasure() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void maySacrificeAnotherArtifactDrawsAndCreatesTreasure() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new TheSackvilleBagginses()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void sacrificingATokenMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new TheSackvilleBagginses());
        addTreasureToken(player1);
        int lifeBefore = gd.getLife(player2.getId());

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    private void addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."));
        harness.addToBattlefield(player, treasureCard);
    }
}
