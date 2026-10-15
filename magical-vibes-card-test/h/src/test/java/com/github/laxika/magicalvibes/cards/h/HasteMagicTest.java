package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HasteMagic.class, IronGiant.class, Mountain.class})
class HasteMagicTest extends BaseCardTest {

    private Card putOnTop() {
        Card card = new Mountain();
        gd.playerDecks.get(player1.getId()).addFirst(card);
        return card;
    }

    @Test
    @DisplayName("Boosts the target creature, grants haste, and exiles the top card")
    void boostsGrantsHasteAndExilesTopCard() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Temporary creature effects expire during cleanup")
    void creatureEffectsExpireDuringCleanup() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Mountain")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void permissionEndsWhenYourEndStepBeginsButCreatureEffectsRemain() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void canPlayExiledLandDuringMainPhase() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        Permanent creature = addCreatureReady(player2, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void opponentsEndStepDoesNotExpirePermission() {
        harness.forceActivePlayer(player2);
        Permanent creature = addCreatureReady(player2, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void emptyLibraryDoesNotPreventCreatureEffects() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void illegalTargetPreventsExilingTopCard() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        harness.assertInGraveyard(player1, "Haste Magic");
    }

    @Test
    void cannotCastExiledInstantOnceYourEndStepBegins() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = new HasteMagic();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledSpellStillRequiresItsManaCost() {
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = new HasteMagic();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, topCard.getId(), creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(6);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void castDuringYourEndStepKeepsPermissionThroughFollowingMainPhase() {
        harness.forceStep(TurnStep.END_STEP);
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void opponentsExtraTurnDoesNotExpirePermissionBeforeYourNextTurn() {
        harness.forceActivePlayer(player2);
        gd.queueExtraTurnFirst(player2.getId(), false);
        Permanent creature = addCreatureReady(player2, new IronGiant());
        Card topCard = putOnTop();
        harness.setHand(player1, List.of(new HasteMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Mountain");
    }
}
