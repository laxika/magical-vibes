package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentOfHeroism.class, AbbeyGriffin.class})
class MomentOfHeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Moment of Heroism puts it on the stack")
    void castingPutsOnStack() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = griffin.getId();
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Moment of Heroism");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Moment of Heroism gives +2/+2 and lifelink to target creature")
    void resolvingBoostsAndGrantsLifelink() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = griffin.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(2);
        assertThat(griffin.getToughnessModifier()).isEqualTo(2);
        assertThat(griffin.getEffectivePower()).isEqualTo(4);
        assertThat(griffin.getEffectiveToughness()).isEqualTo(4);
        assertThat(griffin.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = griffin.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
        assertThat(griffin.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Moment of Heroism fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = griffin.getId();
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Moment of Heroism");
    }

    @Test
    @DisplayName("Lifelink gains life for the targeted creature's controller, even an opponent")
    void opposingCreatureGainsLifeForItsController() {
        Permanent griffin = addCreatureReady(player2, new AbbeyGriffin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Repeated casts stack the boost but lifelink gains life only once per damage")
    void repeatedCastsDoNotMultiplyLifelink() {
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MomentOfHeroism(), new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, griffin.getId());
        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
