package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesSpiral.class, RuneclawBear.class, Fog.class, LavaAxe.class,
        Forest.class, HowlingMine.class, Pacifism.class, GarrukWildspeaker.class})
class NaturesSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Nature's Spiral returns target creature card from graveyard to hand")
    void returnsTargetCreatureFromGraveyardToHand() {
        Card creature = new RuneclawBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Nature's Spiral");
    }

    @Test
    @DisplayName("Nature's Spiral cannot target instant card in graveyard")
    void cannotTargetInstantCardInGraveyard() {
        Card instant = new Fog();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nature's Spiral cannot target sorcery card in graveyard")
    void cannotTargetSorceryCardInGraveyard() {
        Card sorcery = new LavaAxe();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nature's Spiral cannot target card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card creature = new RuneclawBear();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Nature's Spiral fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card creature = new RuneclawBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Casting Nature's Spiral puts a graveyard-targeted sorcery spell on the stack")
    void castingPutsGraveyardTargetedSpellOnStack() {
        Card creature = new RuneclawBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Nature's Spiral returns a land card to hand")
    void returnsLandToHand() {
        assertReturnsOnlyChosenCard(new Forest());
    }

    @Test
    @DisplayName("Nature's Spiral returns a noncreature artifact card to hand")
    void returnsArtifactToHand() {
        assertReturnsOnlyChosenCard(new HowlingMine());
    }

    @Test
    @DisplayName("Nature's Spiral returns an Aura to hand without requiring a creature to enchant")
    void returnsAuraToHand() {
        assertReturnsOnlyChosenCard(new Pacifism());
    }

    @Test
    @DisplayName("Nature's Spiral returns a planeswalker card to hand")
    void returnsPlaneswalkerToHand() {
        assertReturnsOnlyChosenCard(new GarrukWildspeaker());
    }

    @Test
    @DisplayName("Nature's Spiral cannot be cast without a target")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertReturnsOnlyChosenCard(Card target) {
        Card other = new RuneclawBear();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(target.getId());
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(other.getId()).doesNotContain(target.getId());
        harness.assertInGraveyard(player1, "Nature's Spiral");
        harness.assertNotOnBattlefield(player1, target.getName());
    }
}
