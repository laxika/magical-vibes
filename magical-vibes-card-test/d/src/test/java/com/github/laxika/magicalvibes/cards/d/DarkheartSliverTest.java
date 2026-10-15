package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkheartSliver.class, SinewSliver.class, SerraSphinx.class,
        ArtificialEvolution.class, Bitterblossom.class})
class DarkheartSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can sacrifice themselves to gain 3 life")
    void grantsLifeGainAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        harness.addToBattlefield(player2, new SinewSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Sinew Sliver");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player2, "Sinew Sliver");
    }

    @Test
    @DisplayName("A Sliver entering after Darkheart Sliver gains the ability")
    void grantsAbilityToSliverEnteringLater() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Sinew Sliver");
    }

    @Test
    @DisplayName("Darkheart Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Darkheart Sliver");
        harness.assertNotOnBattlefield(player1, "Darkheart Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain Darkheart Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SerraSphinx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Darkheart Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SinewSliver());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and life is gained only on resolution")
    void sacrificeIsCostAndLifeGainUsesStack() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Darkheart Sliver");
        harness.assertNotOnBattlefield(player1, "Darkheart Sliver");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("An activated ability resolves after the granting Sliver is sacrificed")
    void pendingAbilitySurvivesLossOfGrantingSliver() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player2, new SinewSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player2, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Darkheart Sliver");
        harness.assertNotOnBattlefield(player2, "Sinew Sliver");
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Darkheart Sliver does not grant abilities while it is a spell on the stack")
    void doesNotGrantAbilityBeforeEnteringBattlefield() {
        harness.addToBattlefield(player1, new SinewSliver());
        harness.castFromHand(player1, new DarkheartSliver(), "{B}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the ability immediately when Darkheart Sliver sacrifices itself")
    void pendingLifeGainDoesNotKeepGrantActive() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SinewSliver());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Darkheart Sliver");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    @DisplayName("A noncreature Sliver permanent also gains the sacrifice ability")
    void grantsAbilityToNoncreatureSliverPermanent() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.assertNotOnBattlefield(player1, "Bitterblossom");
        harness.assertLife(player1, 13);
    }
}
