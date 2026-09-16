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
    @DisplayName("Lumbering Falls enters tapped and can produce green or blue mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new LumberingFalls()));
        harness.playLand(player1, 0);

        Permanent falls = findPermanent(player1, "Lumbering Falls");
        assertThat(falls.isTapped()).isTrue();

        falls.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Lumbering Falls becomes a 3/3 green and blue Elemental with hexproof")
    void animatesIntoHexproofElemental() {
        Permanent falls = addReadyFalls(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, falls)).isTrue();
        assertThat(gqs.isLand(gd, falls)).isTrue();
        assertThat(gqs.getEffectivePower(gd, falls)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, falls)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, falls))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(falls.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, falls, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Lumbering Falls's animation and hexproof end at end of turn")
    void animationAndHexproofEndAtEndOfTurn() {
        Permanent falls = addReadyFalls(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, falls)).isFalse();
        assertThat(gqs.isLand(gd, falls)).isTrue();
        assertThat(falls.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, falls, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReadyFalls(Player player) {
        Permanent permanent = new Permanent(new LumberingFalls());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
