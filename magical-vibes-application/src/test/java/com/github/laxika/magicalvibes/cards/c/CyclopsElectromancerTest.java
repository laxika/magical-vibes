package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyclopsElectromancer.class, Divination.class, GrizzlyBears.class, Shock.class})
class CyclopsElectromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to instant and sorcery cards in its controller's graveyard")
    void dealsDamageForInstantAndSorceryCardsInGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        target.setToughness(8);
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(targetPermanent.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
