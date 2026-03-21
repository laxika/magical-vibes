package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ViciousOfferingTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice kicker and KickerReplacementEffect with -2/-2 base and -5/-5 kicked")
    void hasCorrectEffects() {
        ViciousOffering card = new ViciousOffering();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.hasSacrificeCost() && !ke.hasManaCost());

        assertThat(card.getEffects(EffectSlot.SPELL))
                .hasSize(1)
                .first()
                .isInstanceOfSatisfying(KickerReplacementEffect.class, kre -> {
                    assertThat(kre.baseEffect()).isInstanceOfSatisfying(BoostTargetCreatureEffect.class, base -> {
                        assertThat(base.powerBoost()).isEqualTo(-2);
                        assertThat(base.toughnessBoost()).isEqualTo(-2);
                    });
                    assertThat(kre.kickedEffect()).isInstanceOfSatisfying(BoostTargetCreatureEffect.class, kicked -> {
                        assertThat(kicked.powerBoost()).isEqualTo(-5);
                        assertThat(kicked.toughnessBoost()).isEqualTo(-5);
                    });
                });
    }

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Without kicker — gives -2/-2, kills a 2/2 creature")
    void unkickedKills2Toughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, -2/-2 makes it 0/0 → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without kicker — gives -2/-2, 3/3 creature survives")
    void unkickedDoesNotKill3Toughness() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Hill Giant is 3/3, -2/-2 makes it 1/1 → survives
        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
        assertThat(giant.getPowerModifier()).isEqualTo(-2);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("With kicker — gives -5/-5, kills a 3/3 creature")
    void kickedKills3Toughness() {
        // Need a creature to sacrifice for kicker cost
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificeId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        // Hill Giant is 3/3, -5/-5 makes it -2/-2 → dies
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        // Sacrificed creature should also be gone
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With kicker — sacrificed creature goes to graveyard")
    void kickedSacrificesCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID sacrificeId = harness.getPermanentId(player1, "Hill Giant");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    // ===== Spell goes to graveyard =====

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vicious Offering");
    }
}
