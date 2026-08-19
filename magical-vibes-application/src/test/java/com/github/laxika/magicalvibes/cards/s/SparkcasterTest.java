package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SparkcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only red or green creatures you control, including itself")
    void etbOffersOnlyRedOrGreenCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        harness.addToBattlefield(player2, new RagingGoblin());

        castSparkcaster(player2.getId());
        resolveUntilPermanentChoice();

        GameData gameData = harness.getGameData();
        UUID sparkcasterId = harness.getPermanentId(player1, "Sparkcaster");
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(greenId, redId, sparkcasterId)
                .doesNotContain(blueId);
    }

    @Test
    @DisplayName("ETB returns the chosen red or green creature and deals 1 damage to the target player")
    void etbReturnsChosenCreatureAndDamagesPlayer() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        harness.setLife(player2, 20);

        castSparkcaster(player2.getId());
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, goblinId);

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Sparkcaster");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB damage can target a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 4);

        castSparkcaster(planeswalker.getId());
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparkcaster"));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInHand(player1, "Sparkcaster");
    }

    @Test
    @DisplayName("ETB damage cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSparkcaster();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSparkcaster(UUID targetId) {
        prepareSparkcaster();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void prepareSparkcaster() {
        harness.setHand(player1, List.of(new Sparkcaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveUntilPermanentChoice() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
