package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingCrescendo.class, CopperLonglegs.class, PropheticPrism.class, Forest.class})
class BlazingCrescendoTest extends BaseCardTest {

    private Card putOnTop(Player player) {
        Card card = new Forest();
        harness.setLibrary(player, List.of(card));
        return card;
    }

    @Test
    @DisplayName("Boosts the target creature and exiles the top card with play permission")
    void boostsCreatureAndExilesTopCard() {
        Permanent creature = addCreatureReady(player1, new CopperLonglegs());
        Card topCard = putOnTop(player1);
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(topCard.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn but play permission remains")
    void boostExpiresBeforePlayPermission() {
        Permanent creature = addCreatureReady(player1, new CopperLonglegs());
        Card topCard = putOnTop(player1);
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Prophetic Prism")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void illegalTargetPreventsExiling() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        Card topCard = putOnTop(player1);
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillAllowsBoostingOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void mayPlayExiledLand() {
        Permanent creature = addCreatureReady(player1, new CopperLonglegs());
        Card topCard = putOnTop(player1);
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Permanent creature = addCreatureReady(player1, new CopperLonglegs());
        Card topCard = new CopperLonglegs();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(topCard.getId());
    }

    @Test
    void castingOnOpponentsTurnGrantsPermissionThroughNextOwnTurn() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Card topCard = putOnTop(player1);
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void permissionExpiresAtEndOfImmediatelyFollowingExtraTurn() {
        Permanent creature = addCreatureReady(player1, new CopperLonglegs());
        Card topCard = putOnTop(player1);
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new BlazingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        gd.queueExtraTurnFirst(player1.getId(), false);

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }
}

