package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BronzeCudgels;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrochiMergeKeeper.class, BronzeCudgels.class, ShortCircuit.class})
class OrochiMergeKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("An unmodified Orochi Merge-Keeper adds one green mana")
    void unmodifiedAddsOneGreenMana() {
        addReadyKeeper();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A modified Orochi Merge-Keeper adds two green mana")
    void modifiedAddsTwoGreenMana() {
        Permanent keeper = addReadyKeeper();
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional mana ability is unavailable while unmodified")
    void additionalManaAbilityIsUnavailableWhileUnmodified() {
        addReadyKeeper();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKeeper() {
        return addCreatureReady(player1, new OrochiMergeKeeper());
    }

    @Test
    void modifiedKeeperCanStillChooseOneGreenMana() {
        Permanent keeper = addReadyKeeper();
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(keeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonPowerToughnessCounterAlsoModifiesKeeper() {
        Permanent keeper = addReadyKeeper();
        keeper.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(keeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingLastCounterRemovesAdditionalAbility() {
        Permanent keeper = addReadyKeeper();
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        keeper.untap();
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keeper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void ownAuraModifiesKeeper() {
        Permanent keeper = addReadyKeeper();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(keeper.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void opponentsAuraDoesNotModifyKeeper() {
        Permanent keeper = addReadyKeeper();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        aura.setAttachedTo(keeper.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void opponentsEquipmentModifiesKeeper() {
        Permanent keeper = addReadyKeeper();
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new BronzeCudgels());
        equipment.setAttachedTo(keeper.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void modifiedKeeperCannotUseTapAbilityWithSummoningSickness() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new OrochiMergeKeeper());
        keeper.setSummoningSick(true);
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keeper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
