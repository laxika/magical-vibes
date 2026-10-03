package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfTheDawn.class, WalkingCorpse.class})
class AngelOfTheDawnTest extends BaseCardTest {

    private void castAngel() {
        harness.setHand(player1, List.of(new AngelOfTheDawn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering boosts and grants vigilance to creatures you control, including itself")
    void boostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new WalkingCorpse());

        castAngel();

        Permanent corpse = findPermanent(player1, "Walking Corpse");
        assertThat(corpse.getEffectivePower()).isEqualTo(3);
        assertThat(corpse.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.VIGILANCE)).isTrue();

        Permanent angel = findPermanent(player1, "Angel of the Dawn");
        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new WalkingCorpse());

        castAngel();

        Permanent corpse = findPermanent(player2, "Walking Corpse");
        assertThat(corpse.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WalkingCorpse());

        castAngel();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent corpse = findPermanent(player1, "Walking Corpse");
        assertThat(corpse.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves receive neither benefit")
    void doesNotAffectCreaturesEnteringLater() {
        castAngel();

        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent corpse = findPermanent(player1, "Walking Corpse");
        assertThat(corpse.getEffectivePower()).isEqualTo(2);
        assertThat(corpse.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The entering ability waits for resolution before granting its benefits")
    void benefitsBeginWhenTriggerResolves() {
        harness.setHand(player1, List.of(new AngelOfTheDawn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Angel of the Dawn");
        assertThat(gd.stack).hasSize(1);
        assertThat(angel.getEffectivePower()).isEqualTo(3);
        assertThat(angel.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();

        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.passBothPriorities();

        Permanent corpse = findPermanent(player1, "Walking Corpse");
        assertThat(corpse.getEffectivePower()).isEqualTo(3);
        assertThat(corpse.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.VIGILANCE)).isTrue();
    }
}
