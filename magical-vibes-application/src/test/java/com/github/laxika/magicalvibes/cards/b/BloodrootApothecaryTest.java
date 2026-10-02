package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BloodrootApothecary.class)
class BloodrootApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Treasure for each player")
    void etbCreatesTreasureForControllerAndTargetOpponent() {
        harness.setHand(player1, List.of(new BloodrootApothecary()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Opponent sacrificing a noncreature token gives them two poison counters")
    void opponentSacrificingNoncreatureTokenGivesPoison() {
        harness.addToBattlefield(player1, new BloodrootApothecary());
        Permanent treasure = addTreasureToken(player2);

        int treasureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(treasure);
        harness.activateAbility(player2, treasureIndex, null, null);
        harness.handleListChoice(player2, ManaColor.GREEN.name());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a creature token does not trigger the poison ability")
    void opponentSacrificingCreatureTokenDoesNotGivePoison() {
        harness.addToBattlefield(player1, new BloodrootApothecary());
        Permanent creatureToken = addCreatureToken(player2);

        int tokenIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creatureToken);
        harness.activateAbility(player2, tokenIndex, null, null);
        harness.handleListChoice(player2, ManaColor.GREEN.name());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("ETB cannot target the controller")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new BloodrootApothecary()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."));
        return addToken(player, card);
    }

    private Permanent addCreatureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card();
        card.setName("Creature Token");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "Sacrifice this creature: Add one mana of any color."));
        return addToken(player, card);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
