package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HibernationSliver.class, SpinedSliver.class, SpinedWurm.class,
        ArtificialEvolution.class, Bitterblossom.class})
class HibernationSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can pay 2 life to return themselves to their owner's hand")
    void grantsSelfBounceToAllSlivers() {
        harness.addToBattlefield(player1, new HibernationSliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new SpinedSliver());
        harness.addToBattlefield(player2, new SpinedSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        int ownSliverIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver);
        harness.activateAbility(player1, ownSliverIndex, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Spined Sliver");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player2, "Spined Sliver");
    }

    @Test
    @DisplayName("Hibernation Sliver can return itself to its owner's hand")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Hibernation Sliver");
        harness.assertNotOnBattlefield(player1, "Hibernation Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.addToBattlefield(player1, new SpinedWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability is lost when Hibernation Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HibernationSliver());
        harness.addToBattlefield(player1, new SpinedSliver());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A granted bounce ability resolves after Hibernation Sliver leaves the battlefield")
    void activatedAbilityResolvesAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HibernationSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new SpinedSliver());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Spined Sliver");
    }

    @Test
    @DisplayName("Life is paid on activation and a tapped, summoning-sick Sliver can activate")
    void paysLifeBeforeReturningTappedSliver() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new HibernationSliver());
        sliver.tap();
        sliver.setSummoningSick(true);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Hibernation Sliver");
        harness.assertNotInHand(player1, "Hibernation Sliver");

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Hibernation Sliver");
        harness.assertNotOnBattlefield(player1, "Hibernation Sliver");
    }

    @Test
    @DisplayName("A player with less than 2 life cannot activate the granted ability")
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertOnBattlefield(player1, "Hibernation Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller pays life but a stolen Sliver returns to its owner's hand")
    void returnsStolenSliverToOwner() {
        harness.addToBattlefield(player1, new HibernationSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player2, new SpinedSliver());
        gd.playerBattlefields.get(player2.getId()).remove(sliver);
        gd.playerBattlefields.get(player1.getId()).add(sliver);
        gd.stolenCreatures.put(sliver.getId(), player2.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Spined Sliver");
        harness.assertNotInHand(player1, "Spined Sliver");
        harness.assertNotOnBattlefield(player1, "Spined Sliver");
    }

    @Test
    @DisplayName("Hibernation Sliver does not grant abilities while its spell is on the stack")
    void doesNotGrantAbilityBeforeEnteringBattlefield() {
        harness.addToBattlefield(player1, new SpinedSliver());
        harness.setHand(player1, List.of(new HibernationSliver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Spined Sliver");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hibernation Sliver");
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    @DisplayName("A noncreature kindred Sliver also gains the return ability")
    void grantsAbilityToNoncreatureSliverPermanent() {
        harness.addToBattlefield(player1, new HibernationSliver());
        Permanent kindred = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, kindred.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Bitterblossom");
        harness.assertNotOnBattlefield(player1, "Bitterblossom");
    }
}
