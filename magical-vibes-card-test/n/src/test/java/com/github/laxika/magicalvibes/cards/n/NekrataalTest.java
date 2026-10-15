package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CardColor;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;

@CardUsed({Nekrataal.class, GrizzlyBears.class, BogWraith.class, Ornithopter.class, Forest.class, GloriousAnthem.class})
class NekrataalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Nekrataal does not choose its ETB target")
    void castingDoesNotChooseEtbTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Nekrataal enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Nekrataal");

        // The ETB ability must choose its target as it is put on the stack.
        assertThat(gd.stack).isEmpty();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(targetId);

        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target a nonblack nonartifact creature you control")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == target);
        harness.assertOnBattlefield(player1, "Nekrataal");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nekrataal");
        harness.assertOnBattlefield(player2, "Bog Wraith");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nekrataal");
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nekrataal");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotExplicitlyTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(land.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that becomes black before resolution is no longer a legal target")
    void targetBecomingBlackSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        target.getGrantedColors().add(CardColor.BLACK);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nekrataal's trigger still destroys its target after Nekrataal leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent source = findPermanent(player1, "Nekrataal");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nekrataal");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroyed creature cannot be regenerated")
    void destroyedCreatureCannotBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Give the target a regeneration shield before ETB resolves
        Permanent target = findPermanent(player2, "Grizzly Bears");
        target.setRegenerationShield(1);

        // Resolve ETB — should destroy despite regeneration shield
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible creature survives Nekrataal's ETB")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Grant indestructible to the target before ETB resolves
        Permanent target = findPermanent(player2, "Grizzly Bears");
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB — should not destroy indestructible creature
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("indestructible"));
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Remove target before ETB resolves
        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can cast without a target when no valid creatures on battlefield")
    void canCastWithoutTargetWhenNoValidCreatures() {
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB is skipped when no legal target exists")
    void etbIsSkippedWhenNoLegalTargetExists() {
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Nekrataal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses a target when the ETB trigger is put on the stack")
    void choosesTargetWhenEtbIsPutOnStack() {
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast without target when only black creatures exist")
    void canCastWithoutTargetWhenOnlyBlackCreatures() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("First strike lets Nekrataal defeat a 2/2 blocker")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new Nekrataal());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
