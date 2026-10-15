package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RangersGuile.class, DarkthicketWolf.class, BrimstoneVolley.class})
class RangersGuileTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Ranger's Guile puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Ranger's Guile gives +1/+1 and hexproof to target creature")
    void resolvingBoostsAndGrantsHexproof() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }


    @Test
    @DisplayName("Boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }


    @Test
    @DisplayName("Ranger's Guile fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Ranger's Guile");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Darkthicket Wolf");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing control of the target before resolution makes it illegal")
    void fizzlesIfTargetChangesController() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castInstant(player1, 0, wolf.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wolf);
        gd.playerBattlefields.get(player2.getId()).add(wolf);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
        assertThat(wolf.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ranger's Guile");
    }

    @Test
    @DisplayName("Hexproof makes an opponent's spell already on the stack lose its target")
    void protectsAgainstSpellOnStack() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");

        harness.castInstant(player2, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Brimstone Volley");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof permits another Ranger's Guile controlled by the creature's controller")
    void canTargetOwnHexproofCreature() {
        harness.addToBattlefield(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new RangersGuile(), new RangersGuile()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(wolf.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }
}
