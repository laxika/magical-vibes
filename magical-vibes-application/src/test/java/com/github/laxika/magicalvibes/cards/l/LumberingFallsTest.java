package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LumberingFalls.class)
class LumberingFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Lumbering Falls enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new LumberingFalls()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Lumbering Falls").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lumbering Falls taps for green or blue mana")
    void tapsForGreenOrBlueMana() {
        addFallsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        Permanent falls = gd.playerBattlefields.get(player1.getId()).getFirst();
        falls.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lumbering Falls becomes a 3/3 green and blue Elemental with hexproof")
    void becomesAnimatedWithHexproof() {
        Permanent falls = addFallsReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, falls)).isTrue();
        assertThat(gqs.isCreature(gd, falls)).isTrue();
        assertThat(gqs.getEffectivePower(gd, falls)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, falls)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, falls)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, falls)).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, falls, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Lumbering Falls animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent falls = addFallsReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, falls)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, falls)).isFalse();
        assertThat(gqs.isLand(gd, falls)).isTrue();
        assertThat(gqs.hasKeyword(gd, falls, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addFallsReady(Player player) {
        Permanent falls = new Permanent(new LumberingFalls());
        falls.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(falls);
        return falls;
    }
}
