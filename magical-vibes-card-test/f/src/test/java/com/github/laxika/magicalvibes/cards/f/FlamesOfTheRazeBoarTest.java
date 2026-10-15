package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamesOfTheRazeBoar.class, ColossalDreadmaw.class, HillGiant.class, CrawWurm.class,
        DuskLegionDreadnought.class})
class FlamesOfTheRazeBoarTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetAndTwoToEachOtherCreatureWhenControllerHasPowerFourCreature() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        Permanent source = addReadyCreature(player1, new CrawWurm());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void onlyDealsFourDamageWhenControllerHasNoPowerFourCreature() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void uncrewedVehicleDoesNotEnableTheAdditionalDamage() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        harness.addToBattlefield(player1, new DuskLegionDreadnought());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void powerThreeCreatureDoesNotEnableTheAdditionalDamage() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        addReadyCreature(player1, new HillGiant());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void creatureWithExactlyFourPowerIncludingCountersEnablesTheAdditionalDamage() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        Permanent source = addReadyCreature(player1, new HillGiant());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void lethalDamageToTargetDoesNotStopTheAdditionalDamage() {
        Permanent target = addReadyCreature(player2, new HillGiant());
        Permanent other = addReadyCreature(player2, new ColossalDreadmaw());
        addReadyCreature(player1, new CrawWurm());

        castFlames(player1, target);

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(other.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void checksThePowerConditionAtResolution() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        Permanent source = addReadyCreature(player1, new CrawWurm());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FlamesOfTheRazeBoar()));
        addMana(player1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void missingTargetPreventsBothDamageSteps() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        addReadyCreature(player1, new CrawWurm());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FlamesOfTheRazeBoar()));
        addMana(player1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Flames of the Raze-Boar");
    }

    @Test
    void canOnlyTargetAnOpponentsCreature() {
        Permanent ownCreature = addReadyCreature(player1, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FlamesOfTheRazeBoar()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFlames(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new FlamesOfTheRazeBoar()));
        addMana(caster);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
