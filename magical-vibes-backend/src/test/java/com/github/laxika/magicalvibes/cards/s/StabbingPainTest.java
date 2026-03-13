package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StabbingPainTest extends BaseCardTest {

    @Test
    @DisplayName("Stabbing Pain has correct effects")
    void hasCorrectEffects() {
        StabbingPain card = new StabbingPain();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(TapTargetPermanentEffect.class);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(boost.powerBoost()).isEqualTo(-1);
        assertThat(boost.toughnessBoost()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Resolving gives -1/-1 and taps target creature")
    void resolvingGivesMinusAndTaps() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant")).findFirst().orElseThrow();
        assertThat(giant.getPowerModifier()).isEqualTo(-1);
        assertThat(giant.getToughnessModifier()).isEqualTo(-1);
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature survives -1/-1 if toughness is high enough")
    void creatureSurvivesIfToughnessHighEnough() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Grizzly Bears is 2/2, -1/-1 makes it 1/1 — it survives
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Stabbing Pain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stabbing Pain"));
    }
}
