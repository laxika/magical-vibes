package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({BattleflightEagle.class, WalkingCorpse.class})
class BattleflightEagleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new BattleflightEagle()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Walking Corpse");
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
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new BattleflightEagle()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();

        Permanent bears = gqs.findPermanentById(gd, targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new BattleflightEagle()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent bears = gqs.findPermanentById(gd, targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new BattleflightEagle()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gqs.findPermanentById(gd, targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new BattleflightEagle()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // ETB on stack

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities(); // Resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target itself when entering an otherwise empty battlefield")
    void canTargetItself() {
        harness.castFromHand(player1, new BattleflightEagle(), "{4}{W}");
        harness.passBothPriorities();

        Permanent eagle = findPermanent(player1, "Battleflight Eagle");
        harness.handlePermanentChosen(player1, eagle.getId());
        resolveAllTriggers();

        assertThat(eagle.getEffectivePower()).isEqualTo(4);
        assertThat(eagle.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(eagle.getEffectivePower()).isEqualTo(2);
        assertThat(eagle.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, eagle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast still boosts the target and grants flying")
    void enteringWithoutCastingTriggersAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.enterBattlefieldAndReturn(player1, new BattleflightEagle());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Trigger resolves even after Battleflight Eagle leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.castFromHand(player1, new BattleflightEagle(), "{4}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent eagle = findPermanent(player1, "Battleflight Eagle");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, eagle));
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Battleflight Eagle");
    }
}
