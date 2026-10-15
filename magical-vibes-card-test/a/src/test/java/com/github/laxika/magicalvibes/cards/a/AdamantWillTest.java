package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MesaUnicorn;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.cards.v.ViciousOffering;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdamantWill.class, MesaUnicorn.class, Eviscerate.class, ShivanFire.class, ViciousOffering.class})
class AdamantWillTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Adamant Will puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Adamant Will");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Adamant Will gives +2/+2 and indestructible to target creature")
    void resolvingBoostsAndGrantsIndestructible() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Adamant Will can boost an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Adamant Will protects a creature from destruction")
    void protectsFromDestruction() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill(), new Eviscerate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player1, "Mesa Unicorn");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Adamant Will protects against lethal damage and cleanup removes damage safely")
    void protectsFromLethalDamageThroughCleanup() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill(), new ShivanFire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mesa Unicorn");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mesa Unicorn");
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getId()).isEqualTo(targetId);
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible does not protect a creature with zero or less toughness")
    void doesNotProtectFromNegativeToughness() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.setHand(player2, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        UUID sacrificeId = harness.getPermanentId(player2, "Mesa Unicorn");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castKickedInstantWithSacrifice(player2, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mesa Unicorn");
    }

    @Test
    @DisplayName("Adamant Will fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Mesa Unicorn");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Adamant Will");
    }
}
