package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EvanescentIntellect;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.StasisField;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DranaAndLinvala.class, DrudgeSkeletons.class, LlanowarElves.class, ProdigalPyromancer.class,
        StasisField.class, EvanescentIntellect.class})
class DranaAndLinvalaTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of creatures opponents control")
    void blocksOpponentsCreatureAbilities() {
        addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Drana and Linvala");
    }

    @Test
    @DisplayName("Gains an activated ability from an opponent's creature")
    void gainsOpponentsCreatureAbility() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dranaAndLinvala.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains a creature's tap-for-mana ability")
    void gainsOpponentsCreatureManaAbility() {
        addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain activated abilities from creatures its controller controls")
    void ignoresControllersCreatures() {
        addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player1, new DrudgeSkeletons());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayCopiedColoredCostWithColorlessMana() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dranaAndLinvala.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void blocksOpponentsManaAbilities() {
        addCreatureReady(player1, new DranaAndLinvala());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(elves.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void controllersCreaturesCanStillActivateAbilities() {
        addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void copiedTapAbilityUsesDranaAndLinvalaAsSource() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(dranaAndLinvala.isTapped()).isTrue();
        assertThat(pyromancer.isTapped()).isFalse();
    }

    @Test
    void copiedTapAbilityRequiresDranaAndLinvalaToNotBeSummoningSick() {
        harness.addToBattlefield(player1, new DranaAndLinvala());
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiedAbilityResolvesAfterDonorLeavesBattlefield() {
        addCreatureReady(player1, new DranaAndLinvala());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player2.getId()).remove(pyromancer);
        gd.playerGraveyards.get(player2.getId()).add(pyromancer.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void losesCopiedAbilityWhenDonorLeavesBattlefield() {
        addCreatureReady(player1, new DranaAndLinvala());
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        gd.playerBattlefields.get(player2.getId()).remove(skeletons);
        gd.playerGraveyards.get(player2.getId()).add(skeletons.getCard());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCopyPrintedAbilitiesOfFaceDownCreatures() {
        addCreatureReady(player1, new DranaAndLinvala());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        pyromancer.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAbilitiesStopsLockingOpponentsNonManaAbilities() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new StasisField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, dranaAndLinvala.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void losingAbilitiesStopsLockingOpponentsManaAbilities() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new StasisField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, dranaAndLinvala.getId());
        harness.passBothPriorities();

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void manaColorPermissionDoesNotApplyToAnUnrelatedAuraGrantedAbility() {
        Permanent dranaAndLinvala = addCreatureReady(player1, new DranaAndLinvala());
        harness.setHand(player1, List.of(new EvanescentIntellect()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, dranaAndLinvala.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dranaAndLinvala.isTapped()).isFalse();
    }
}
