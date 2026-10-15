package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightspearZealot.class, Ornithopter.class})
class BrightspearZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after its controller casts a second spell this turn")
    void getsBoostAfterSecondSpell() {
        harness.setHand(player1, List.of(new BrightspearZealot(), new Ornithopter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent zealot = findZealot();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost before its controller casts a second spell")
    void hasNoBoostAfterOneSpell() {
        harness.setHand(player1, List.of(new BrightspearZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zealot = findZealot();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost applies as soon as the second spell is cast, before it resolves")
    void getsBoostWhileSecondSpellIsOnStack() {
        harness.addToBattlefield(player1, new BrightspearZealot());
        harness.setHand(player1, List.of(new BrightspearZealot(), new BrightspearZealot()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Permanent zealot = findZealot();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counts spells cast before entry, stays at +2/+0 after further spells, and resets next turn")
    void countsEarlierSpellsWithoutStackingAndResetsNextTurn() {
        harness.setHand(player1, List.of(new BrightspearZealot(), new BrightspearZealot(),
                new BrightspearZealot()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> zealots = findPermanents(player1, "Brightspear Zealot");
        assertThat(zealots).hasSize(2);
        for (Permanent zealot : zealots) {
            assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
        }

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        for (Permanent zealot : findPermanents(player1, "Brightspear Zealot")) {
            assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        }

        harness.passUntil(player2, TurnStep.UPKEEP);

        for (Permanent zealot : findPermanents(player1, "Brightspear Zealot")) {
            assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("An opponent's spells do not enable the boost")
    void ignoresOpponentSpells() {
        harness.addToBattlefield(player1, new BrightspearZealot());
        harness.setHand(player2, List.of(new BrightspearZealot(), new BrightspearZealot()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findZealot())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findZealot())).isEqualTo(4);
        for (Permanent zealot : findPermanents(player2, "Brightspear Zealot")) {
            assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Attacking does not tap Brightspear Zealot")
    void attacksWithoutTapping() {
        Permanent zealot = addCreatureReady(player1, new BrightspearZealot());

        declareAttackers(List.of(0));

        assertThat(zealot.isTapped()).isFalse();
        harness.assertLife(player2, 18);
    }

    private Permanent findZealot() {
        return findPermanent(player1, "Brightspear Zealot");
    }
}
