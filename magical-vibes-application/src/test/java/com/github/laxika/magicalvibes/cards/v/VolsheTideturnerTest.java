package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolsheTideturner.class, Unsummon.class, GrizzlyBears.class, AcademyDrake.class})
class VolsheTideturnerTest extends BaseCardTest {

    @Test
    void tappingAddsBlueManaRestrictedToInstantsSorceriesOrKickedSpells() {
        Permanent tideturner = addReadyTideturner();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana())
                .isEqualTo(1);
        assertThat(tideturner.isTapped()).isTrue();
    }

    @Test
    void restrictedBlueManaPaysForAnInstant() {
        addReadyTideturner();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedBlueManaCannotPayForACreatureSpell() {
        addReadyTideturner();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void restrictedBlueManaPaysForAKickedCreatureSpell() {
        addReadyTideturner();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getKickedOrInstantSorceryOnlyManaTotal()).isZero();
    }

    private Permanent addReadyTideturner() {
        Permanent tideturner = harness.addToBattlefieldAndReturn(player1, new VolsheTideturner());
        tideturner.setSummoningSick(false);
        return tideturner;
    }
}
