package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FloodpitsDrowner;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarvinMurderousMimic.class, DrudgeSkeletons.class, LlanowarElves.class,
        FloodpitsDrowner.class, Millstone.class})
class MarvinMurderousMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from differently named creatures you control")
    void gainsOwnCreatureAbility() {
        Permanent marvin = addCreatureReady(player1, new MarvinMurderousMimic());
        addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(marvin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains a creature's tap-for-mana ability")
    void gainsOwnCreatureManaAbility() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain activated abilities from creatures an opponent controls")
    void ignoresOpponentsCreatures() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        addCreatureReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiedAbilityResolvesAfterDonorLeaves() {
        Permanent marvin = addCreatureReady(player1, new MarvinMurderousMimic());
        Permanent donor = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, donor);
        harness.passBothPriorities();

        assertThat(marvin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void losesAbilityWhenDonorLeaves() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        Permanent donor = addCreatureReady(player1, new DrudgeSkeletons());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, donor);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCopyAbilitiesDonorHasLost() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        Permanent donor = addCreatureReady(player1, new DrudgeSkeletons());
        donor.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSicknessStopsCopiedTapAbility() {
        harness.addToBattlefield(player1, new MarvinMurderousMimic());
        addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void donorsSummoningSicknessDoesNotStopMarvinsManaAbility() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void doesNotCopyNoncreatureAbilities() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiedShuffleAbilityMovesMarvinInsteadOfDonor() {
        addCreatureReady(player1, new MarvinMurderousMimic());
        Permanent donor = addCreatureReady(player1, new FloodpitsDrowner());
        Permanent target = addCreatureReady(player2, new FloodpitsDrowner());
        target.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marvin, Murderous Mimic");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(donor);
        assertThat(donor.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .contains("Marvin, Murderous Mimic");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(Card::getName))
                .contains("Floodpits Drowner");
    }
}
