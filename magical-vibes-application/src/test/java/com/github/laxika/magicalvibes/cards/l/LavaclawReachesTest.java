package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LavaclawReachesTest extends BaseCardTest {

    @Test
    @DisplayName("Lavaclaw Reaches enters tapped and adds black or red mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new LavaclawReaches()));
        harness.playLand(player1, 0);

        Permanent reaches = findPermanent(player1, "Lavaclaw Reaches");
        assertThat(reaches.isTapped()).isTrue();

        reaches.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Lavaclaw Reaches becomes a 2/2 black and red Elemental that is still a land")
    void animatesIntoElemental() {
        Permanent reaches = addReadyReaches(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, reaches)).isTrue();
        assertThat(gqs.isLand(gd, reaches)).isTrue();
        assertThat(gqs.getEffectivePower(gd, reaches)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reaches)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, reaches))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(reaches.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Granted ability gives Lavaclaw Reaches +X/+0 until end of turn")
    void grantedAbilityBoostsPower() {
        Permanent reaches = addReadyReaches(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 2, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaches)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reaches)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animation and granted ability end at end of turn")
    void animationAndGrantedAbilityEndAtEndOfTurn() {
        Permanent reaches = addReadyReaches(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, reaches)).isFalse();
        assertThat(gqs.isLand(gd, reaches)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 1, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyReaches(Player player) {
        Permanent permanent = new Permanent(new LavaclawReaches());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
