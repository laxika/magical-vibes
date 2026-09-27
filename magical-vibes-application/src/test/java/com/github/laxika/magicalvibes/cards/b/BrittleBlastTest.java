package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrittleBlast.class, DoomBlade.class, GrizzlyBears.class})
class BrittleBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and exiles opposing creatures that would die")
    void grantsPerpetualExileInsteadOfDying() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.exiledCards).anyMatch(entry -> entry.card().getId().equals(firstTarget.getCard().getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(firstTarget.getCard().getId()));

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gameData.exiledCards).anyMatch(entry -> entry.card().getId().equals(secondTarget.getCard().getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(secondTarget.getCard().getId()));
    }

    @Test
    @DisplayName("Does not grant the replacement effect to the caster's creatures")
    void doesNotGrantToOwnCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gameData.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
