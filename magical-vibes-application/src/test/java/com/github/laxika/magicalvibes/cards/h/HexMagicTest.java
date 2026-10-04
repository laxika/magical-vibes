package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KreeCommandos;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, KreeCommandos.class, HexMagic.class})
class HexMagicTest extends BaseCardTest {

    @Test
    void exilesHandDrawsSameNumberAndGrantsPlayPermission() {
        Card exiledLand = new Forest();
        Card exiledCreature = new KreeCommandos();
        Card drawnLand = new Forest();
        Card drawnCreature = new KreeCommandos();
        harness.setLibrary(player1, List.of(drawnLand, drawnCreature));
        harness.setHand(player1, List.of(new HexMagic(), exiledLand, exiledCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand, drawnCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(exiledLand, exiledCreature);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(exiledLand.getId(), player1.getId())
                .containsEntry(exiledCreature.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(exiledLand.getId(), player1.getId())
                .containsEntry(exiledCreature.getId(), player1.getId());
    }

    @Test
    void playsExiledLandAndCreatureWithNormalCosts() {
        Card exiledLand = new Forest();
        Card exiledCreature = new KreeCommandos();
        harness.setHand(player1, List.of(new HexMagic(), exiledLand, exiledCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castFromExile(player1, exiledLand.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledLand)
                .anyMatch(permanent -> permanent.getCard() == exiledCreature);
    }

    @Test
    void emptyHandOnResolutionDoesNotDrawOrExileAnything() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new HexMagic(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void permissionLastsThroughNextTurnAndLeavesUnplayedCardsExiled() {
        Card land = resolveWithExiledLand();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void extraTurnIsTheNextTurnForPermissionExpiry() {
        Card land = resolveWithExiledLand();
        gd.queueExtraTurnFirst(player1.getId(), false);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    void permissionDoesNotWaiveManaCostsOrSorceryTiming() {
        Card creature = new KreeCommandos();
        harness.setHand(player1, List.of(new HexMagic(), creature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    void permissionDoesNotGrantAnAdditionalLandPlay() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setHand(player1, List.of(new HexMagic(), firstLand, secondLand));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castFromExile(player1, firstLand.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondLand);
    }

    private Card resolveWithExiledLand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HexMagic(), land));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        return land;
    }
}
