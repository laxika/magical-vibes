package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellTheMighty.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class})
class FellTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with power greater than the target creature's power")
    void destroysCreaturesWithGreaterPowerThanTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new HowlingMine());

        castFellTheMighty(target);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player1, "Fell the Mighty");
    }

    @Test
    @DisplayName("Requires a creature target")
    void requiresCreatureTarget() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Howling Mine")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castFellTheMighty(Permanent target) {
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
