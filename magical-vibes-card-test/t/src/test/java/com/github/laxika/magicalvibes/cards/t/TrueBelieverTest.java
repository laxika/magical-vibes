package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrueBeliever.class, Shock.class, GlorySeeker.class, GoblinSharpshooter.class})
class TrueBelieverTest extends BaseCardTest {

    @Test
    @DisplayName("Casting True Believer puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new TrueBeliever(), "{W}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(TrueBeliever.class);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new TrueBeliever()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts True Believer onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new TrueBeliever(), "{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "True Believer");
    }

    @Test
    @DisplayName("True Believer enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new TrueBeliever(), "{W}{W}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "True Believer");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target player with a spell when True Believer is on battlefield")
    void opponentCannotTargetPlayerWithSpell() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Player can still target themselves with a spell when opponent has True Believer")
    void canTargetSelfWhenOpponentHasShroud() {
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Player with True Believer cannot be targeted even by their own spells")
    void cannotTargetSelfWithSpellWhenOwnTrueBelieverOnField() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Player can be targeted after True Believer is removed from battlefield")
    void canTargetPlayerAfterTrueBelieverRemoved() {
        TrueBeliever believer = new TrueBeliever();
        harness.addToBattlefield(player1, believer);

        // Remove True Believer from battlefield
        GameData gd = harness.getGameData();
        Permanent perm = findPermanent(player1, "True Believer");
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(believer);

        // Now the player can be targeted
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Opponent cannot target player with an ability when True Believer is on battlefield")
    void opponentCannotTargetPlayerWithAbility() {
        harness.addToBattlefield(player1, new TrueBeliever());
        addCreatureReady(player2, new GoblinSharpshooter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("True Believer grants shroud to the player, not to creatures")
    void shroudProtectsPlayerNotCreatures() {
        harness.addToBattlefield(player1, new TrueBeliever());
        Permanent creature = addCreatureReady(player1, new GlorySeeker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Multiple True Believers on battlefield still grant shroud")
    void multipleTrueBelieversStillGrantShroud() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.addToBattlefield(player1, new TrueBeliever());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Removing one True Believer when two are on battlefield still protects player")
    void removingOneTrueBelieverStillProtectsWhenTwoExist() {
        harness.addToBattlefield(player1, new TrueBeliever());
        harness.addToBattlefield(player1, new TrueBeliever());

        // Remove one True Believer
        GameData gd = harness.getGameData();
        Permanent firstBeliever = findPermanent(player1, "True Believer");
        gd.playerBattlefields.get(player1.getId()).remove(firstBeliever);

        // Player still has shroud from the second True Believer
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Own activated abilities cannot target a player with shroud")
    void ownAbilityCannotTargetProtectedPlayer() {
        harness.addToBattlefield(player1, new TrueBeliever());
        addCreatureReady(player1, new GoblinSharpshooter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A spell does not resolve against a player who gains shroud before resolution")
    void gainingShroudMakesSpellTargetIllegal() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());

        harness.addToBattlefield(player1, new TrueBeliever());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("An ability does not resolve against a player who gains shroud before resolution")
    void gainingShroudMakesAbilityTargetIllegal() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new GoblinSharpshooter());
        harness.activateAbility(player1, 0, null, player1.getId());

        harness.addToBattlefield(player1, new TrueBeliever());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("True Believer itself can be targeted and destroyed")
    void trueBelieverCanBeTargeted() {
        Permanent believer = harness.addToBattlefieldAndReturn(player1, new TrueBeliever());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, believer.getId());

        harness.assertNotOnBattlefield(player1, "True Believer");
        harness.assertInGraveyard(player1, "True Believer");
    }

    @Test
    @DisplayName("Shroud follows True Believer's current controller")
    void shroudFollowsCurrentController() {
        Permanent believer = harness.addToBattlefieldAndReturn(player1, new TrueBeliever());
        gd.playerBattlefields.get(player1.getId()).remove(believer);
        gd.playerBattlefields.get(player2.getId()).add(believer);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}
