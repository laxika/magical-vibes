package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjaniMentorOfHeroes;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfTolvada;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TheBrokenSky;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, Plains.class, RodOfRuin.class, SuntailHawk.class})
class RodOfRuinTest extends BaseCardTest {
    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RodOfRuin(), "{4}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new RodOfRuin(), "{4}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rod of Ruin");
    }
    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability targeting creature puts it on the stack")
    void activatingTargetingCreaturePutsOnStack() {
        addCreatureReady(player1, new RodOfRuin());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Rod of Ruin")
    void activatingTapsRod() {
        Permanent rod = addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(rod.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target self to deal 1 damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent rod = addCreatureReady(player1, new RodOfRuin());
        rod.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        rod.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(rod.isTapped()).isTrue();
    }
    @CardUsed(AjaniMentorOfHeroes.class)
    @Test
    @DisplayName("Target enumeration offers creatures, planeswalkers, and players but no other permanents")
    void targetEnumerationExcludesNonCreaturePermanents() {
        Permanent rod = addCreatureReady(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniMentorOfHeroes());

        GameData gd = harness.getGameData();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        var response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, rod.getCard(), rod.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), 0);

        // "Any target" is a creature, planeswalker, battle, or player — the land and the Rod itself
        // are not legal, and activation would reject them.
        assertThat(response.validPermanentIds()).contains(bearId, planeswalker.getId()).doesNotContain(plainsId, rod.getId());
        assertThat(response.validPlayerIds()).contains(player1.getId(), player2.getId());
    }

    @CardUsed(AjaniMentorOfHeroes.class)
    @Test
    @DisplayName("Deals 1 damage to a target planeswalker")
    void deals1DamageToPlaneswalker() {
        addCreatureReady(player1, new RodOfRuin());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniMentorOfHeroes());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @CardUsed({InvasionOfTolvada.class, TheBrokenSky.class})
    @Test
    @DisplayName("Deals 1 damage to a target battle")
    void deals1DamageToBattle() {
        addCreatureReady(player1, new RodOfRuin());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfTolvada());
        battle.setCounterCount(CounterType.DEFENSE, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Tolvada");
    }

    @CardUsed({AwakenedSkyclave.class, InvasionOfZendikar.class})
    @Test
    @DisplayName("Deals 1 damage to a target battle")
    void deals1DamageToBattleUpstreamReview() {
        addCreatureReady(player1, new RodOfRuin());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability targeting an artifact is rejected")
    void cannotTargetArtifact() {
        Permanent rod = addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rod.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }
    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addCreatureReady(player1, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
