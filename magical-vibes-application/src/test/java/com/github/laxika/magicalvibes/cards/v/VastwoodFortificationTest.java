package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.t.TajuruBlightblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VastwoodFortification.class, VastwoodThicket.class, Forest.class,
        TajuruBlightblade.class, IntoTheRoil.class})
class VastwoodFortificationTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TajuruBlightblade());
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void spellFaceCannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceEntersTappedAndProducesGreenMana() {
        harness.setHand(player1, List.of(new VastwoodFortification()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(VastwoodThicket.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutCounterOnOwnCreatureDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TajuruBlightblade());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Vastwood Fortification");
    }

    @Test
    void doesNotPutCounterOnTargetReturnedToHandInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TajuruBlightblade());
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Tajuru Blightblade");
        harness.assertInHand(player2, "Tajuru Blightblade");
        harness.assertInGraveyard(player1, "Vastwood Fortification");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLandFaceCannotProduceManaBeforeUntapping() {
        harness.setHand(player1, List.of(new VastwoodFortification()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void landFaceUsesNormalLandPlayAllowance() {
        harness.setHand(player1, List.of(new VastwoodFortification(), new Forest()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void landFaceCannotBePlayedDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Vastwood Fortification");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
