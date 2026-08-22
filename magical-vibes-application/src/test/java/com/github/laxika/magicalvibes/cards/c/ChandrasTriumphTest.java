package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasTriumph.class, AirElemental.class, ChandraNalaar.class})
class ChandrasTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to an opponent's creature without a Chandra")
    void dealsThreeDamageWithoutChandra() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(target);

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals 5 damage to an opponent's creature while controlling Chandra")
    void dealsFiveDamageWithChandra() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ChandrasTriumph()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ChandrasTriumph()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
