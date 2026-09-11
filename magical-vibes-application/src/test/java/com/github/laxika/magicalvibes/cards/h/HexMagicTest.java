package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, HexMagic.class})
class HexMagicTest extends BaseCardTest {

    @Test
    void exilesHandDrawsSameNumberAndGrantsPlayPermission() {
        Card exiledLand = new Forest();
        Card exiledCreature = new GrizzlyBears();
        Card drawnLand = new Forest();
        Card drawnCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnLand, drawnCreature));
        harness.setHand(player1, List.of(new HexMagic(), exiledLand, exiledCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand, drawnCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(exiledLand, exiledCreature);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(exiledLand.getId(), player1.getId())
                .containsEntry(exiledCreature.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(exiledLand.getId(), gd.turnNumber + 2)
                .containsEntry(exiledCreature.getId(), gd.turnNumber + 2);
    }

    @Test
    void playsExiledLandAndCreatureWithNormalCosts() {
        Card exiledLand = new Forest();
        Card exiledCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new HexMagic(), exiledLand, exiledCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCardFromExile(gd, player1, exiledCreature.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledLand)
                .anyMatch(permanent -> permanent.getCard() == exiledCreature);
    }
}
