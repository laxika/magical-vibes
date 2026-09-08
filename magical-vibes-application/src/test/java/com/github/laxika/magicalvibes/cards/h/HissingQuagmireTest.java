package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed(HissingQuagmire.class)
class HissingQuagmireTest extends BaseCardTest {

    @Test
    @DisplayName("Hissing Quagmire enters tapped and can produce black or green mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new HissingQuagmire()));
        harness.playLand(player1, 0);

        Permanent quagmire = findPermanent(player1, "Hissing Quagmire");
        assertThat(quagmire.isTapped()).isTrue();

        quagmire.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Hissing Quagmire becomes a 2/2 black and green Elemental with deathtouch")
    void animatesIntoDeathtouchElemental() {
        Permanent quagmire = addReadyQuagmire(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quagmire)).isTrue();
        assertThat(gqs.isLand(gd, quagmire)).isTrue();
        assertThat(gqs.getEffectivePower(gd, quagmire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, quagmire)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, quagmire))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(quagmire.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, quagmire, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Hissing Quagmire's animation and deathtouch end at end of turn")
    void animationAndDeathtouchEndAtEndOfTurn() {
        Permanent quagmire = addReadyQuagmire(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quagmire)).isFalse();
        assertThat(gqs.isLand(gd, quagmire)).isTrue();
        assertThat(quagmire.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, quagmire, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addReadyQuagmire(Player player) {
        Permanent permanent = new Permanent(new HissingQuagmire());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
