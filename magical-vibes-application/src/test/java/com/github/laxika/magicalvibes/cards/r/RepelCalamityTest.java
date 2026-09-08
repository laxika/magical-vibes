package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepelCalamity.class, AirElemental.class, WallOfTanglecord.class, GrizzlyBears.class, Plains.class})
class RepelCalamityTest extends BaseCardTest {

    @Test
    void destroysCreatureWithPowerAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void destroysCreatureWithToughnessAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfTanglecord());

        cast(target);

        harness.assertInGraveyard(player2, "Wall of Tanglecord");
    }

    @Test
    void cannotTargetCreatureWithPowerAndToughnessBelowFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power or toughness 4 or greater");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        setUpSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void cast(Permanent target) {
        setUpSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void setUpSpell() {
        harness.setHand(player1, List.of(new RepelCalamity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
