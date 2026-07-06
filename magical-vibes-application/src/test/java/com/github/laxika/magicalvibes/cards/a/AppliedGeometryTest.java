package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Soliton;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

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
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target Aura permanents")
    void cannotTargetAura() {
        com.github.laxika.magicalvibes.model.Card aura = new com.github.laxika.magicalvibes.model.Card();
        aura.setName("Test Aura");
        aura.setType(CardType.ENCHANTMENT);
        aura.setSubtypes(List.of(CardSubtype.AURA));
        aura.setManaCost("{1}{W}");

        harness.addToBattlefield(player1, aura);
        harness.setHand(player1, List.of(new AppliedGeometry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Test Aura");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
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
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
