package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrochiMergeKeeper.class})
class OrochiMergeKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("An unmodified Orochi Merge-Keeper adds one green mana")
    void unmodifiedAddsOneGreenMana() {
        addReadyKeeper();

        harness.getGameService().tapPermanent(gd, player1, 0);

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
}
