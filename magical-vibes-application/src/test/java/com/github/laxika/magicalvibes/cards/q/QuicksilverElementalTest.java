package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.d.Duskworker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverElemental.class, Duskworker.class, AuriokTransfixer.class,
        AncientDen.class, CopperMyr.class})
class QuicksilverElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a target creature's named activated ability")
    void gainsTargetCreatureActivatedAbility() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverElemental());
        Permanent duskworker = addCreatureReady(player1, new Duskworker());

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, 0, null, duskworker.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, quicksilver)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blue mana pays a copied colored activated ability")
    void paysCopiedColoredAbilityWithBlueMana() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverElemental());
        Permanent transfixer = addCreatureReady(player1, new AuriokTransfixer());
        Permanent ancientDen = harness.addToBattlefieldAndReturn(player1, new AncientDen());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, transfixer.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, ancientDen.getId());
        harness.passBothPriorities();

        assertThat(quicksilver.isTapped()).isTrue();
        assertThat(ancientDen.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Gains a creature's tap-for-mana ability")
    void gainsCreatureTapForManaAbility() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverElemental());
        Permanent copperMyr = addCreatureReady(player1, new CopperMyr());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, copperMyr.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(quicksilver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new QuicksilverElemental());
        Permanent ancientDen = harness.addToBattlefieldAndReturn(player1, new AncientDen());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ancientDen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gained activated abilities expire at cleanup")
    void gainedAbilitiesExpireAtCleanup() {
        addCreatureReady(player1, new QuicksilverElemental());
        Permanent duskworker = addCreatureReady(player1, new Duskworker());

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, 0, null, duskworker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
