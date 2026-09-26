package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.k.KodamaOfTheNorthTree;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntaidakeTheCloudKeeper.class, KodamaOfTheNorthTree.class, HumbleBudoka.class})
class UntaidakeTheCloudKeeperTest extends BaseCardTest {

    private void activateManaAbility() {
        harness.activateAbility(player1, 0, null, null);
    }

    private ManaPool pool() {
        return gd.playerManaPools.get(player1.getId());
    }

    @Test
    @DisplayName("Untaidake enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UntaidakeTheCloudKeeper()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Untaidake, the Cloud Keeper").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating taps Untaidake, pays 2 life and adds two legendary-only colorless")
    void activatingAddsRestrictedManaAndPaysLife() {
        Permanent untaidake = harness.addToBattlefieldAndReturn(player1, new UntaidakeTheCloudKeeper());
        harness.setLife(player1, 20);

        activateManaAbility();

        assertThat(untaidake.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(pool().get(ManaColor.COLORLESS)).isZero();
        assertThat(pool().getLegendarySpellOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("The restricted mana pays the generic cost of a legendary spell")
    void paysLegendarySpell() {
        harness.addToBattlefield(player1, new UntaidakeTheCloudKeeper());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new KodamaOfTheNorthTree()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kodama of the North Tree");
        assertThat(pool().getLegendarySpellOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("The restricted mana cannot pay for a nonlegendary spell")
    void cannotPayNonlegendarySpell() {
        harness.addToBattlefield(player1, new UntaidakeTheCloudKeeper());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new HumbleBudoka()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pool().getLegendarySpellOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Untaidake cannot be activated without 2 life")
    void cannotActivateWithoutTwoLife() {
        Permanent untaidake = harness.addToBattlefieldAndReturn(player1, new UntaidakeTheCloudKeeper());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> activateManaAbility())
                .isInstanceOf(IllegalStateException.class);

        assertThat(untaidake.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(pool().getLegendarySpellOnlyColorless()).isZero();
    }
}
