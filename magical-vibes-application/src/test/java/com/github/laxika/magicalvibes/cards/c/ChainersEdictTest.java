package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainersEdict.class, GrizzlyBears.class})
class ChainersEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature")
    void targetPlayerSacrificesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chainer's Edict");
    }

    @Test
    @DisplayName("Flashback sacrifices a creature and exiles Chainer's Edict")
    void flashbackSacrificesCreatureAndExilesSpell() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, java.util.List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Chainer's Edict");
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chainer's Edict"));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
