package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.n.NoxiousNewt;
import com.github.laxika.magicalvibes.cards.i.Interjection;
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

@CardUsed({ChaseInspiration.class, NoxiousNewt.class, Interjection.class})
class ChaseInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Chase Inspiration puts it on the stack")
    void castingPutsOnStack() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = newt.getId();
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chase Inspiration");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Chase Inspiration gives +0/+3 and hexproof to target creature")
    void resolvingBoostsAndGrantsHexproof() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = newt.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(newt.getPowerModifier()).isEqualTo(0);
        assertThat(newt.getToughnessModifier()).isEqualTo(3);
        assertThat(newt.getEffectivePower()).isEqualTo(1);
        assertThat(newt.getEffectiveToughness()).isEqualTo(5);
        assertThat(newt.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Cannot target opponent's creature with Chase Inspiration")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Noxious Newt");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = newt.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(newt.getPowerModifier()).isEqualTo(0);
        assertThat(newt.getToughnessModifier()).isEqualTo(0);
        assertThat(newt.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Chase Inspiration fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = newt.getId();
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Chase Inspiration");
    }

    @Test
    @DisplayName("Granted hexproof prevents an opponent from targeting the creature")
    void hexproofPreventsOpponentTargeting() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, newt.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Interjection()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, newt.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Your own spells can target the creature after it gains hexproof")
    void hexproofAllowsOwnTargeting() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new ChaseInspiration(), new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, newt.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, newt.getId());
        harness.passBothPriorities();

        assertThat(newt.getPowerModifier()).isZero();
        assertThat(newt.getToughnessModifier()).isEqualTo(6);
        assertThat(newt.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Granting hexproof in response makes an opponent's spell fail to resolve")
    void hexproofInvalidatesSpellAlreadyOnStack() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player2, List.of(new Interjection()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, newt.getId());

        harness.setHand(player1, List.of(new ChaseInspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, newt.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(newt.getPowerModifier()).isZero();
        assertThat(newt.getToughnessModifier()).isEqualTo(3);
        assertThat(newt.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.assertInGraveyard(player2, "Interjection");
    }
}
