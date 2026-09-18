package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessBlaze.class, GrizzlyBears.class, ColossalDreadmaw.class, Shock.class})
class RecklessBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana only for creatures you control that die from the blast")
    void addsManaForYourDamagedCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBlaze();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Remembers creatures that survive the blast and die later that turn")
    void triggersForLaterDeath() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());

        castBlaze();

        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.castInstant(player1, 0, dreadmaw.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
    }

    private void castBlaze() {
        harness.setHand(player1, java.util.List.of(new RecklessBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
