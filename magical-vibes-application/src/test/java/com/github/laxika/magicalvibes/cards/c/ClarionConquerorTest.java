package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClarionConqueror.class, RodOfRuin.class, ProdigalPyromancer.class,
        ChandraNalaar.class, SolRing.class, LlanowarElves.class, Forest.class, TurnToFrog.class})
class ClarionConquerorTest extends BaseCardTest {

    @Test
    void blocksMatchingPermanentAbilities() {
        harness.addToBattlefield(player1, new ClarionConqueror());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer()).setSummoningSick(false);
        harness.addToBattlefield(player2, new ChandraNalaar());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        for (int index = 0; index < 3; index++) {
            int permanentIndex = index;
            assertThatThrownBy(() -> harness.activateAbility(player2, permanentIndex, null, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated")
                    .hasMessageContaining("Clarion Conqueror");
        }
    }

    @Test
    void blocksMatchingPermanentManaAbilities() {
        harness.addToBattlefield(player1, new ClarionConqueror());
        harness.addToBattlefield(player2, new SolRing());
        harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> harness.tapPermanent(player2, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void allowsLandAbilities() {
        harness.addToBattlefield(player1, new ClarionConqueror());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removalReenablesAbilities() {
        Permanent clarion = harness.addToBattlefieldAndReturn(player1, new ClarionConqueror());
        harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer()).setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(clarion);
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void blocksItsControllersAbilitiesToo() {
        harness.addToBattlefield(player1, new ClarionConqueror());
        harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer()).setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void losingAbilitiesReenablesActivatedAbilities() {
        Permanent clarion = harness.addToBattlefieldAndReturn(player1, new ClarionConqueror());
        harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer()).setSummoningSick(false);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, clarion.getId());
        harness.activateAbility(player1, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void losingAbilitiesReenablesManaAbilities() {
        Permanent clarion = harness.addToBattlefieldAndReturn(player1, new ClarionConqueror());
        harness.addToBattlefieldAndReturn(player1, new LlanowarElves()).setSummoningSick(false);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, clarion.getId());
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void enteringDoesNotCounterAnAlreadyActivatedAbility() {
        harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer()).setSummoningSick(false);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.addToBattlefield(player1, new ClarionConqueror());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
