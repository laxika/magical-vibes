package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LacerateFlesh.class, AirElemental.class, GrizzlyBears.class})
class LacerateFleshTest extends BaseCardTest {

    @Test
    void createsBloodTokensForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void createsNoBloodTokensWhenDamageIsNotExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new LacerateFlesh()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LacerateFlesh()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
