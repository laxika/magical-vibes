package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CelestialColonnadeTest extends BaseCardTest {

    @Test
    @DisplayName("Celestial Colonnade enters tapped and adds white or blue mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new CelestialColonnade()));
        harness.playLand(player1, 0);

        Permanent colonnade = findPermanent(player1, "Celestial Colonnade");
        assertThat(colonnade.isTapped()).isTrue();

        colonnade.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Celestial Colonnade becomes a 4/4 white and blue Elemental with flying and vigilance")
    void animatesIntoCelestialColonnade() {
        Permanent colonnade = addReadyColonnade(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colonnade)).isTrue();
        assertThat(gqs.isLand(gd, colonnade)).isTrue();
        assertThat(gqs.getEffectivePower(gd, colonnade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colonnade)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, colonnade))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(colonnade.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, colonnade, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, colonnade, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Celestial Colonnade stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent colonnade = addReadyColonnade(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colonnade)).isFalse();
        assertThat(gqs.isLand(gd, colonnade)).isTrue();
        assertThat(colonnade.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, colonnade, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, colonnade, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addReadyColonnade(Player player) {
        Permanent permanent = new Permanent(new CelestialColonnade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
