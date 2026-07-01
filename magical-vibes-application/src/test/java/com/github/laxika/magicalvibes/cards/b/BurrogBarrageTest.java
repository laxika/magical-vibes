package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCastAnotherSpellThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurrogBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Spell has conditional boost and bite effects with multi-target")
    void cardStructure() {
        BurrogBarrage card = new BurrogBarrage();
        var effects = card.getEffects(EffectSlot.SPELL);

        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(ControllerCastAnotherSpellThisTurnConditionalEffect.class);
        assertThat(effects.get(1)).isInstanceOf(FirstTargetDealsPowerDamageToSecondTargetEffect.class);

        var conditional = (ControllerCastAnotherSpellThisTurnConditionalEffect) effects.get(0);
        assertThat(conditional.wrapped()).isInstanceOf(BoostFirstTargetCreatureEffect.class);
        assertThat(((BoostFirstTargetCreatureEffect) conditional.wrapped()).powerBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without another instant or sorcery cast, bite uses base power and no boost")
    void biteWithoutPriorInstantOrSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("After casting another instant, creature gets +1/+0 and boosted bite kills")
    void boostAndBiteAfterPriorInstant() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GiantGrowth(), new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Prior creature spell does not satisfy the instant/sorcery condition")
    void priorCreatureSpellDoesNotEnableBoost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new LlanowarElves(), new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID bearId = bf.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst()
                .orElseThrow();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        Permanent bear = bf.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(bear.getPowerModifier()).isZero();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can resolve with only the first target (up to one opponent creature)")
    void biteOptionalSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        harness.assertInGraveyard(player1, "Burrog Barrage");
    }

    @Test
    @DisplayName("Bite deals damage but does not kill a tougher creature")
    void biteDamagesButDoesNotKill() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target opponent creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new BurrogBarrage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
