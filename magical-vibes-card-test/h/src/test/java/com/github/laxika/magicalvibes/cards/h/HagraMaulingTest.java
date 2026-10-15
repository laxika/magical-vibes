package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExpeditionSkulker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HagraMauling.class, HagraBroodpit.class, Forest.class, ExpeditionSkulker.class})
class HagraMaulingTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Expedition Skulker");
    }

    @Test
    void costsOneLessWhenOpponentsControlNoBasicLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void doesNotGetCostReductionWhenOpponentControlsBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellFaceCannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysFullCostWhenOpponentControlsBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Expedition Skulker");
        harness.assertInGraveyard(player1, "Hagra Mauling");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void opponentsNonbasicLandDoesNotPreventCostReduction() {
        harness.addToBattlefield(player2, new HagraBroodpit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Expedition Skulker");
        harness.assertOnBattlefield(player2, "Hagra Broodpit");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void castersBasicLandDoesNotPreventCostReductionOrTargetingOwnCreature() {
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Expedition Skulker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void reductionDoesNotRemoveBlackManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());
        harness.setHand(player1, List.of(new HagraMauling()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceCannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new HagraMauling()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void landFaceUsesTheLandPlayForTheTurn() {
        harness.setHand(player1, List.of(new HagraMauling(), new Forest()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hagra Broodpit");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new HagraMauling()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(HagraBroodpit.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }
}
