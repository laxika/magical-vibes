package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Soliton;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppliedGeometry.class, GrizzlyBears.class, Soliton.class, Pacifism.class, Forest.class})
class AppliedGeometryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 6/6 Fractal token copy of target creature you control")
    void createsFractalTokenCopyOfCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.FRACTAL);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(0);
        assertThat(token.getCard().getToughness()).isEqualTo(0);
        assertThat(token.getPlusOnePlusOneCounters()).isEqualTo(6);
    }

    @Test
    @DisplayName("Creates a Fractal creature token copy of target artifact you control")
    void createsFractalTokenCopyOfArtifact() {
        harness.addToBattlefield(player1, new Soliton());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Soliton");
        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soliton") && p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FRACTAL);
        assertThat(token.getPlusOnePlusOneCounters()).isEqualTo(6);
        assertThat(token.getCard().getActivatedAbilities()).isNotEmpty();
    }

    @Test
    @DisplayName("Cannot target opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target Aura permanents")
    void cannotTargetAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = aura.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Copies a land as a Fractal creature without copying its tapped state or counters")
    void copiesLandWithoutPermanentState() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Forest());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, original.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FOREST, CardSubtype.FRACTAL);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getPlusOnePlusOneCounters()).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(original.getPlusOnePlusOneCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not copy a target that is no longer controlled by the caster")
    void fizzlesWhenTargetChangesController() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, original.getId());

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(original);
    }
}
