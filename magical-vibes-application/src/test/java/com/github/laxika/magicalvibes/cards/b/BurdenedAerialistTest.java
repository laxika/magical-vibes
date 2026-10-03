package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AcademyManufactor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurdenedAerialist.class, AcademyManufactor.class})
class BurdenedAerialistTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Treasure token")
    void enteringCreatesTreasure() {
        harness.setHand(player1, List.of(new BurdenedAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Gains flying until end of turn when a Treasure is sacrificed")
    void gainsFlyingWhenTreasureIsSacrificed() {
        Permanent aerialist = addCreatureReady(player1, new BurdenedAerialist());
        addTreasureToken(player1);

        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void gainsFlyingWhenFoodTokenIsSacrificed() {
        createManufactorTokens();
        Permanent aerialist = findPermanent(player1, "Burdened Aerialist");
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.assertLife(player1, 23);
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsFlyingWhenClueTokenIsSacrificed() {
        createManufactorTokens();
        Permanent aerialist = findPermanent(player1, "Burdened Aerialist");
        Permanent clue = findPermanent(player1, "Clue");
        harness.setLibrary(player1, List.of(new BurdenedAerialist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGainFlyingWhenOpponentSacrificesTreasure() {
        Permanent aerialist = addCreatureReady(player1, new BurdenedAerialist());
        Permanent treasure = addTreasureToken(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player2, "RED");
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private void createManufactorTokens() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new BurdenedAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setManaCost("");
        treasureCard.setToken(true);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));
        return addCreatureReady(player, treasureCard);
    }
}
