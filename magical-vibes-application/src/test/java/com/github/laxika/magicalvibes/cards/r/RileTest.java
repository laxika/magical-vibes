package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rile.class, DeeprootWarrior.class, JungleDelver.class})
class RileTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rile puts it on the stack targeting a creature you control")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Deeproot Warrior");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Rile");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Rile deals 1 damage, grants trample, and draws a card")
    void resolvingDealsOneDamageGrantsTramplAndDraws() {
        harness.addToBattlefield(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Deeproot Warrior");
        harness.castSorcery(player1, 0, targetId);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature with Rile")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Deeproot Warrior");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Deeproot Warrior");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rile goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Deeproot Warrior");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rile");
    }

    @Test
    @DisplayName("Rile fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Deeproot Warrior");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rile");
        // Should NOT draw a card when fizzling
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Rile still grants trample and draws when its damage is prevented")
    void preventedDamageStillGrantsTrampleAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        creature.setDamagePreventionShield(1);
        harness.setHand(player1, List.of(new Rile()));
        harness.setLibrary(player1, List.of(new DeeprootWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Jungle Delver");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Deeproot Warrior");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Rile draws a card even when its damage is lethal to the creature")
    void lethalDamageStillDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new Rile()));
        harness.setLibrary(player1, List.of(new DeeprootWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Jungle Delver");
        harness.assertInGraveyard(player1, "Jungle Delver");
        harness.assertInHand(player1, "Deeproot Warrior");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Rile does nothing if its target changes controller before resolution")
    void targetChangingControllerStopsAllEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
        harness.setHand(player1, List.of(new Rile()));
        harness.setLibrary(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Rile");
        assertThat(gd.stack).isEmpty();
    }
}
