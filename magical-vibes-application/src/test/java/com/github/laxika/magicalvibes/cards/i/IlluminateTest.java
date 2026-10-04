package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.f.FerventCharge;
import com.github.laxika.magicalvibes.cards.p.PenumbraWurm;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Illuminate.class, Dodecapod.class, FerventCharge.class, PenumbraWurm.class, RoostOfDrakes.class})
class IlluminateTest extends BaseCardTest {

    @Test
    void dealsXDamageToTargetCreatureWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(2, 1, 0, 0);

        cast(target.getId(), 2, false, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void redKickerAlsoDamagesTargetCreaturesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(4, 2, 0, 0);

        cast(target.getId(), 2, true, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void blueKickerDrawsXCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm()));
        addMana(5, 1, 0, 1);

        cast(target.getId(), 2, false, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void bothKickersResolveTheirIndependentEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm()));
        addMana(7, 2, 0, 1);

        cast(target.getId(), 2, true, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void redKickerDamagesTheCurrentControllerOfTheTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(4, 2, 0, 0);

        cast(target.getId(), 2, true, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void blueKickerCannotBePaidMoreThanOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(8, 1, 0, 2);

        assertThatThrownBy(() -> cast(target.getId(), 2, false, List.of("{3}{U}", "{3}{U}")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXDealsNoDamageAndDrawsNoCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm()));
        addMana(3, 1, 0, 1);

        cast(target.getId(), 0, false, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FerventCharge());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(2, 1, 0, 0);

        assertThatThrownBy(() -> cast(target.getId(), 2, false, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lethalDamageStillDamagesTheCreaturesControllerAndDrawsCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm(), new PenumbraWurm()));
        addMana(8, 2, 0, 1);

        cast(target.getId(), 3, true, List.of("{3}{U}"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void illegalOnlyTargetPreventsBothKickerEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm()));
        addMana(7, 2, 0, 1);

        cast(target.getId(), 2, true, List.of("{3}{U}"));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Illuminate");
    }

    @Test
    @CardUsed({Illuminate.class, Dodecapod.class, PenumbraWurm.class, RoostOfDrakes.class})
    void blueKickerAloneTriggersKickedSpellAbilities() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new PenumbraWurm(), new PenumbraWurm()));
        addMana(5, 1, 0, 1);

        cast(target.getId(), 2, false, List.of("{3}{U}"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drake");
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void addMana(int colorless, int red, int green, int blue) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, red);
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.BLUE, blue);
    }

    private void cast(java.util.UUID targetId, int xValue, boolean kicked, List<String> additionalCosts) {
        if (!kicked && additionalCosts.isEmpty()) {
            harness.castSorcery(player1, 0, xValue, targetId);
            return;
        }
        gs.playCard(gd, player1, 0, xValue, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                additionalCosts, false);
    }
}
