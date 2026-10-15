package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.ShinkaTheBloodsoakedKeep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorWornShaku.class, IsamaruHoundOfKonda.class, HumbleBudoka.class, ShinkaTheBloodsoakedKeep.class})
class HonorWornShakuTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Honor-Worn Shaku adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new HonorWornShaku());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Honor-Worn Shaku").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping a legendary permanent untaps Honor-Worn Shaku, letting it make mana again")
    void tapLegendaryUntapsShaku() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());

        harness.tapPermanent(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Honor-Worn Shaku").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot untap Honor-Worn Shaku with only a nonlegendary permanent")
    void cannotUntapWithNonlegendaryPermanent() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new HumbleBudoka());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot untap Honor-Worn Shaku when the only legendary permanent is already tapped")
    void cannotUntapWithTappedLegendary() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        findPermanent(player1, "Isamaru, Hound of Konda").tap();

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only a legendary permanent controlled by the Shaku's controller can pay the cost")
    void opponentLegendaryPermanentCannotPayCost() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player2, "Isamaru, Hound of Konda").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability can be activated while Honor-Worn Shaku is already untapped")
    void canActivateUntapAbilityWhileShakuIsUntapped() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Honor-Worn Shaku").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick legendary creature taps as a cost before the untap ability resolves")
    void summoningSickCreaturePaysCostBeforeResolution() {
        var shaku = harness.addToBattlefieldAndReturn(player1, new HonorWornShaku());
        var isamaru = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        isamaru.setSummoningSick(true);
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(isamaru.isTapped()).isTrue();
        assertThat(shaku.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(shaku.isTapped()).isFalse();
        assertThat(isamaru.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A legendary land can pay the untap cost without activating its mana ability")
    void legendaryLandPaysUntapCostWithoutProducingMana() {
        var shaku = harness.addToBattlefieldAndReturn(player1, new HonorWornShaku());
        var shinka = harness.addToBattlefieldAndReturn(player1, new ShinkaTheBloodsoakedKeep());
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaku.isTapped()).isFalse();
        assertThat(shinka.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The untap ability untaps only the Shaku that activated it")
    void untapsOnlySourceShaku() {
        var firstShaku = harness.addToBattlefieldAndReturn(player1, new HonorWornShaku());
        var secondShaku = harness.addToBattlefieldAndReturn(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstShaku.isTapped()).isTrue();
        assertThat(secondShaku.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
    }
}
