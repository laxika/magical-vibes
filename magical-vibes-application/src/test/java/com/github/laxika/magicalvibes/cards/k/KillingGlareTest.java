package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KillingGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power X or less")
    void destroysCreatureWithinPowerLimit() {
        Permanent target = addCreature(player2, 2, 2);

        castKillingGlare(2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than X")
    void rejectsCreatureAbovePowerLimit() {
        Permanent target = addCreature(player2, 3, 3);

        assertThatThrownBy(() -> castKillingGlare(2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Checks the target's power again when it resolves")
    void fizzlesIfTargetBecomesTooPowerful() {
        Permanent target = addCreature(player2, 2, 2);

        castKillingGlare(2, target);
        target.setPowerModifier(target.getPowerModifier() + 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
    }

    @Test
    @DisplayName("X=0 can destroy a creature with zero power")
    void zeroXDestroysZeroPowerCreature() {
        Permanent target = addCreature(player2, 0, 4);

        castKillingGlare(0, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void castKillingGlare(int xValue, Permanent target) {
        harness.setHand(player1, List.of(new KillingGlare()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 1);
        harness.castInstant(player1, 0, xValue, target.getId());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, int power, int toughness) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
