package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekApothecary.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class, Shock.class})
class WojekApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Shields the target and every creature sharing a color with it")
    void shieldsTargetAndColorSharingCreatures() {
        Permanent apothecary = addReadyApothecary();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        activate(apothecary, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(matchingCreature.getDamagePreventionShield()).isEqualTo(1);
        assertThat(differentColorCreature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents the next damage to each affected creature")
    void preventsNextDamageToEachAffectedCreature() {
        Permanent apothecary = addReadyApothecary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        activate(apothecary, target);

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, matchingCreature.getId());
        harness.castAndResolveInstant(player1, 0, differentColorCreature.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A colorless target does not shield other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent apothecary = addReadyApothecary();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activate(apothecary, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(otherColorlessCreature.getDamagePreventionShield()).isZero();
        assertThat(coloredCreature.getDamagePreventionShield()).isZero();
    }

    private Permanent addReadyApothecary() {
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new WojekApothecary());
        apothecary.setSummoningSick(false);
        return apothecary;
    }

    private void activate(Permanent apothecary, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apothecary),
                null, target.getId());
        harness.passBothPriorities();
    }
}
