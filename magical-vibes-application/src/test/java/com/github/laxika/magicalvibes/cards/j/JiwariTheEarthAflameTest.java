package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.e.EbonyOwlNetsuke;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiwariTheEarthAflame.class, InnerChamberGuard.class, ArabaMothrider.class,
        EbonyOwlNetsuke.class})
class JiwariTheEarthAflameTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsXDamageToTargetCreatureWithoutFlying() {
        Permanent jiwari = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        assertThat(jiwari.isTapped()).isTrue();
    }

    @Test
    void channelDealsXDamageToEachCreatureWithoutFlyingAndDiscardsSource() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        addCreatureReady(player1, new InnerChamberGuard());
        addCreatureReady(player1, new ArabaMothrider());
        addCreatureReady(player2, new InnerChamberGuard());
        addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jiwari, the Earth Aflame");
        harness.assertNotOnBattlefield(player1, "Inner-Chamber Guard");
        harness.assertNotOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertOnBattlefield(player1, "Araba Mothrider");
        harness.assertOnBattlefield(player2, "Araba Mothrider");
    }

    @Test
    void battlefieldAbilityCannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    void battlefieldAbilityCannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EbonyOwlNetsuke());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
