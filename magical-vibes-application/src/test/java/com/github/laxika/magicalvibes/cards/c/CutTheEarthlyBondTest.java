package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.f.FreedFromTheReal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutTheEarthlyBond.class, FreedFromTheReal.class, ArabaMothrider.class})
class CutTheEarthlyBondTest extends BaseCardTest {

    @Test
    void returnsTargetEnchantedPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(target.getId());

        harness.setHand(player1, List.of(new CutTheEarthlyBond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Araba Mothrider");
        harness.assertNotOnBattlefield(player2, "Araba Mothrider");
        harness.assertInGraveyard(player1, "Freed from the Real");
        harness.assertInGraveyard(player1, "Cut the Earthly Bond");
    }

    @Test
    void cannotTargetAnUnenchantedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());

        harness.setHand(player1, List.of(new CutTheEarthlyBond()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted permanent");
    }

    @Test
    void fizzlesIfTargetIsNoLongerEnchanted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(target.getId());

        harness.setHand(player1, List.of(new CutTheEarthlyBond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertNotInHand(player2, "Araba Mothrider");
    }
}
