package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainLanneryStorm.class, CostlyPlunder.class, SailorOfMeans.class})
class CaptainLanneryStormTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure artifact token")
    void attackCreatesTreasureToken() {
        addCreatureReady(player1, new CaptainLanneryStorm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Treasure")
                        && p.getCard().getType() == CardType.ARTIFACT
                        && p.getCard().getSubtypes().contains(CardSubtype.TREASURE)
                        && p.getCard().isToken());
    }

    @Test
    @DisplayName("Treasure token has activated ability for mana")
    void treasureTokenHasActivatedAbility() {
        addCreatureReady(player1, new CaptainLanneryStorm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Treasure");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gets +1/+0 when a Treasure is sacrificed")
    void boostsWhenTreasureSacrificed() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        // Create a Treasure token by attacking
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));

        // Move to main phase so we can activate abilities
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Activate the Treasure token's ability (tap + sacrifice → add mana)
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");

        // The Treasure sacrifice trigger should have fired, putting +1/+0 on stack
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing multiple Treasures gives cumulative +1/+0 boosts")
    void multiTreasureSacrificeGivesCumulativeBoost() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        // Manually create two Treasure tokens
        addTreasureToken(player1);
        addTreasureToken(player1);

        int firstTreasure = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));

        // Sacrifice first treasure
        harness.activateAbility(player1, firstTreasure, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        int remainingTreasure = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, remainingTreasure, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a non-Treasure does not trigger +1/+0")
    void nonTreasureSacrificeDoesNotTrigger() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());
        Permanent sailor = addCreatureReady(player1, new SailorOfMeans());
        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstantWithSacrifice(player1, 0, null, sailor.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        // Sacrificing a creature does not satisfy the Treasure condition.
        assertThat(captain.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+1/+0 modifier resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        addTreasureToken(player1);

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent sacrificing a Treasure does not boost Captain")
    void opponentTreasureDoesNotBoostCaptain() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());
        addTreasureToken(player2);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrificing Treasure as a spell cost also boosts Captain")
    void treasureSacrificedAsSpellCostBoostsCaptain() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, treasure.getId());
        assertThat(captain.getPowerModifier()).isZero();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isZero();
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setManaCost("");
        treasureCard.setToken(true);
        treasureCard.setColor(null);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new com.github.laxika.magicalvibes.model.ActivatedAbility(
                true,
                null,
                List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost(),
                        new com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));
        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
    }
}
