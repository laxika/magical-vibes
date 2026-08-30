package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Polliwallop.class, Frogmite.class, AirElemental.class})
class PolliwallopTest extends BaseCardTest {

    @Test
    void dealsTwiceSourcePowerToOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void opponentFrogsDoNotReduceCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new Frogmite());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotTargetOwnCreatureAsVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void addPolliwallopMana(int colorless) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
