package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Polliwallop.class, Frogmite.class, AirElemental.class, PondProphet.class, DaggerfangDuo.class})
class PolliwallopTest extends BaseCardTest {

    @Test
    void dealsTwiceSourcePowerToOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

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

    @Test
    void cannotCastWithoutVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PondProphet());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void threeFrogsReduceGenericCostToZero() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PondProphet());
        harness.addToBattlefield(player1, new PondProphet());
        harness.addToBattlefield(player1, new PondProphet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(0);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Daggerfang Duo");
        harness.assertOnBattlefield(player1, "Pond Prophet");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void affinityCannotPayGreenManaRequirement() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PondProphet());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new PondProphet());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Polliwallop()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PondProphet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Daggerfang Duo");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageIfSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PondProphet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Polliwallop()));
        addPolliwallopMana(2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Daggerfang Duo");
        assertThat(target.getMarkedDamage()).isZero();
    }

    private void addPolliwallopMana(int colorless) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
