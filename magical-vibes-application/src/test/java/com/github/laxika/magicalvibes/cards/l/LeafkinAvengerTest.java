package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeafkinAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap adds green mana for each creature with power 4 or greater you control")
    void tapAddsGreenManaForBigCreatures() {
        harness.addToBattlefield(player1, new LeafkinAvenger());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent avenger = findPermanent(player1, "Leafkin Avenger");
        avenger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven generic and red mana makes the Avenger deal damage equal to its power")
    void dealsPowerDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LeafkinAvenger());

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage ability cannot target a creature")
    void damageAbilityCannotTargetCreature() {
        harness.addToBattlefield(player1, new LeafkinAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
