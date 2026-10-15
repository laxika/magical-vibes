package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnerFlameAcolyte.class, WoodlandChangeling.class})
class InnerFlameAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB gives target creature +2/+0 and haste; Acolyte stays")
    void hardcastBoostsAndGrantsHaste() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreature(player1, 0, List.of(targetId));
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Inner-Flame Acolyte");
    }

    @Test
    @DisplayName("Boost and haste wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreature(player1, 0, List.of(targetId));
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Evoke: target still gets +2/+0 and haste, Acolyte is sacrificed by its separate trigger")
    void evokeSacrificesSelf() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Inner-Flame Acolyte - sacrifice this creature");
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Inner-Flame Acolyte");
        harness.assertInGraveyard(player1, "Inner-Flame Acolyte");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities(); // ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB -> fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
    @Test
    @DisplayName("Evoke sacrifice still resolves when the boost target disappears")
    void evokeSacrificesEvenWhenTargetDisappears() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Inner-Flame Acolyte - sacrifice this creature");
        gd.playerBattlefields.get(player2.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Inner-Flame Acolyte");
        harness.assertInGraveyard(player1, "Inner-Flame Acolyte");
    }

    @Test
    @DisplayName("ETB can boost a creature controlled by its controller")
    void boostsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new InnerFlameAcolyte()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Inner-Flame Acolyte");
    }
}
