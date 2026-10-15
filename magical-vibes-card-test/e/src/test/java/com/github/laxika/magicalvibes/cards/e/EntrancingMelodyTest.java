package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KinjallisCaller;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.q.QueensCommission;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EntrancingMelody.class, RaptorCompanion.class, KinjallisCaller.class,
        QueensCommission.class, LightningStrike.class, Willbender.class, Island.class})
class EntrancingMelodyTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of target creature with mana value equal to X")
    void gainsControlWithMatchingManaValue() {
        // Raptor Companion has mana value 2
        UUID raptorId = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=2, plus {U}{U} = 4 total mana needed
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, raptorId);
        harness.passBothPriorities();

        // Creature should now be controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(raptorId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(raptorId));

        // Control is permanent
        assertThat(gd.newestControlEffectFor(raptorId).duration()).isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
    }

    @Test
    @DisplayName("Cannot target creature with mana value different from X")
    void cannotTargetCreatureWithWrongManaValue() {
        // Raptor Companion has mana value 2, but we'll cast with X=1
        UUID raptorId = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // 4 mana so X=2 is affordable and the spell has a legal target to be cast at all;
        // the announced X=1 is what makes this particular target illegal.
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, raptorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("Cannot target creature with mana value 2 when X=0")
    void cannotTargetWhenXIsZeroAndManaValueIsHigher() {
        UUID raptorId = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // 4 mana so X=2 is affordable and the spell has a legal target to be cast at all;
        // the announced X=0 is what makes this particular target illegal.
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, raptorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("Gains control of 1-MV creature with X=1")
    void gainsControlOfOneManaValueCreature() {
        // Kinjalli's Caller has mana value 1 ({W})
        UUID callerId = harness.addToBattlefieldAndReturn(player2, new KinjallisCaller()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=1, plus {U}{U} = 3 total mana
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 1, callerId);
        harness.passBothPriorities();

        // Should now be controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(callerId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(callerId));
    }

    @Test
    @DisplayName("Castable at a smaller X than the caster could afford")
    void castableAtSmallerXThanAffordable() {
        // Only a 1-MV creature is around, so X=1 is the announcement that has a legal target even
        // though 4 mana would pay for X=2.
        UUID callerId = harness.addToBattlefieldAndReturn(player2, new KinjallisCaller()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 1, callerId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(callerId));
    }

    @Test
    @DisplayName("Cannot target 1-MV creature with X=2")
    void cannotTargetOneManaValueCreatureWithXTwo() {
        // Kinjalli's Caller has mana value 1, but X=2
        UUID callerId = harness.addToBattlefieldAndReturn(player2, new KinjallisCaller()).getId();

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=2, plus {U}{U} = 4 total mana
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, callerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("X=0 gains control of an opponent's creature token")
    void gainsControlOfTokenWithXZero() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new QueensCommission()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        UUID tokenId = harness.getPermanentId(player2, "Vampire");

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0, tokenId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(tokenId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(tokenId));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Control lasts beyond the end of the turn")
    void controlDoesNotExpireAtEndOfTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion()).getId();
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(targetId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetId));
    }

    @Test
    @DisplayName("Gaining control leaves a tapped creature tapped and summoning sick")
    void doesNotUntapOrGrantHaste() {
        var target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        target.tap();
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Can target a creature already controlled by the caster")
    void canTargetOwnCreatureWithoutResettingSummoningSickness() {
        var target = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.isSummoningSick()).isFalse();
        harness.assertInGraveyard(player1, "Entrancing Melody");
    }

    @Test
    @DisplayName("Does not gain control when the target dies before resolution")
    void doesNotGainControlOfRemovedTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion()).getId();
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 2, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Raptor Companion");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Raptor Companion");
        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInGraveyard(player1, "Entrancing Melody");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A face-down morph creature can be stolen with X=0")
    void gainsControlOfFaceDownCreatureWithXZero() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Willbender()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        var target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.isFaceDown()).isTrue();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("A face-down morph creature does not have its printed mana value")
    void cannotTargetFaceDownCreatureWithPrintedManaValue() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Willbender()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even when its mana value matches X")
    void cannotTargetNoncreatureWithMatchingManaValue() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }
}
