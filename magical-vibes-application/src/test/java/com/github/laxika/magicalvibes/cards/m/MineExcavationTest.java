package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MineExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card from graveyard to hand")
    void returnsTargetArtifactToHand() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Returns a target enchantment card from graveyard to hand")
    void returnsTargetEnchantmentToHand() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard; the card returns to its owner's hand")
    void returnsFromOpponentGraveyardToOwnersHand() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // "to its owner's hand" — the opponent's card goes to the opponent's hand, not the caster's.
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card that is neither artifact nor enchantment")
    void cannotTargetPlainCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());

        harness.castWithConspire(player1, 0, artifact.getId(), List.of(hawk.getId(), vanguard.getId()));

        GameData gd = harness.getGameData();
        assertThat(hawk.isTapped()).isTrue();
        assertThat(vanguard.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsColorlessCreature() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent thopter = addCreatureReady(player1, new Ornithopter()); // colorless

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, artifact.getId(),
                List.of(hawk.getId(), thopter.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
