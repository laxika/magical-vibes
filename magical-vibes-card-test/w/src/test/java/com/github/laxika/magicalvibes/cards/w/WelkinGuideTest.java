package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelkinGuide.class, CylianElf.class})
class WelkinGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB gives target creature +2/+2 and flying")
    void etbBoostsAndGrantsFlying() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();

        Permanent bears = permanent(targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Cylian Elf");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent bears = permanent(targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanent(targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // ETB on stack

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities(); // Resolve ETB with an illegal target

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Welkin Guide can choose itself after entering an otherwise empty battlefield")
    void canTargetItself() {
        harness.castFromHand(player1, new WelkinGuide(), "{4}{W}");
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Welkin Guide");
        harness.handlePermanentChosen(player1, guide.getId());
        resolveAllTriggers();

        assertThat(guide.getEffectivePower()).isEqualTo(4);
        assertThat(guide.getEffectiveToughness()).isEqualTo(4);
        assertThat(guide.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB still boosts its target when Welkin Guide leaves before resolution")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new WelkinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Welkin Guide");
        gd.playerBattlefields.get(player1.getId()).remove(guide);
        gd.playerGraveyards.get(player1.getId()).add(guide.getCard());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    private Permanent permanent(UUID id) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }
}
