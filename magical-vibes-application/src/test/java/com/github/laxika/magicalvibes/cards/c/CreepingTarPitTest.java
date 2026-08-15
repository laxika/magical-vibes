package com.github.laxika.magicalvibes.cards.c;

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

class CreepingTarPitTest extends BaseCardTest {

    @Test
    @DisplayName("Creeping Tar Pit enters tapped and adds blue or black mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new CreepingTarPit()));
        harness.playLand(player1, 0);

        Permanent tarPit = findPermanent(player1, "Creeping Tar Pit");
        assertThat(tarPit.isTapped()).isTrue();

        tarPit.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Creeping Tar Pit becomes an unblockable 3/2 blue and black Elemental")
    void animatesAndBecomesUnblockable() {
        Permanent tarPit = addReadyTarPit(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tarPit)).isTrue();
        assertThat(gqs.isLand(gd, tarPit)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tarPit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tarPit)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, tarPit))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(tarPit.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(tarPit.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Creeping Tar Pit's animation and unblockability end at end of turn")
    void animationAndUnblockabilityEndAtEndOfTurn() {
        Permanent tarPit = addReadyTarPit(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tarPit)).isFalse();
        assertThat(gqs.isLand(gd, tarPit)).isTrue();
        assertThat(tarPit.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(tarPit.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyTarPit(Player player) {
        Permanent permanent = new Permanent(new CreepingTarPit());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
