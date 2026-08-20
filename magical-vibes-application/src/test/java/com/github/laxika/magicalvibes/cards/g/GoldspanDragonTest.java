package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoldspanDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure token")
    void attackCreatesTreasureToken() {
        addCreatureReady(player1, new GoldspanDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Becoming the target of a spell creates a Treasure token")
    void beingTargetedBySpellCreatesTreasureToken() {
        Permanent dragon = addCreatureReady(player1, new GoldspanDragon());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, dragon.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Treasures gain an additional ability that produces two mana")
    void treasureGainsTwoManaAbility() {
        addCreatureReady(player1, new GoldspanDragon());
        addTreasureToken(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setManaCost("");
        treasureCard.setToken(true);
        treasureCard.setColor(null);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));
        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
    }
}
