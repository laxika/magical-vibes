package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruesomeEncore.class, GrizzlyBears.class, Pacifism.class, Shock.class, Unsummon.class, Persuasion.class})
class GruesomeEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Gruesome Encore puts it on the stack with graveyard target")
    void castingPutsOnStack() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolving puts creature onto battlefield with haste under caster's control")
    void resolvesAndPutsCreatureOnBattlefield() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        // Creature should be on player1's battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Creature should be removed from player2's graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        // Creature should have haste
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);

        // Creature should be marked for exile at end step
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).contains(new DelayedPermanentAction(creature.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsKey(creature.getId());

        // Creature should be marked with exile-if-leaves replacement
        assertThat(creature.isExileIfLeavesBattlefield()).isTrue();
    }

    @Test
    @DisplayName("Creature is exiled at end step")
    void creatureExiledAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        // Creature should no longer be on the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Creature should be in exile (original owner's)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature is exiled instead of going to graveyard when destroyed by damage")
    void exileReplacementWhenDestroyed() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        UUID creatureId = creature.getId();

        // Reset game state so player2 can cast an instant
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Use Shock to deal 2 damage and destroy Grizzly Bears (2 toughness)
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);

        // Creature should NOT be in any graveyard (replacement redirects to exile)
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        // Creature should be in exile (original owner's)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature is exiled instead of returning to hand when bounced")
    void exileReplacementWhenBounced() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        UUID creatureId = creature.getId();

        // Reset game state so player2 can cast an instant
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Use Unsummon to bounce the creature
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);

        // Creature should NOT be in any hand (replacement redirects to exile)
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        // Creature should be in exile (original owner's)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target creature card in your own graveyard")
    void cannotTargetOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Fizzles if target leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target non-creature card in opponent's graveyard")
    void cannotTargetNonCreature() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Reanimated creature can attack during the turn it enters")
    void hasteAllowsImmediateAttack() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Grizzly Bears").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Delayed exile retains Gruesome Encore as its source")
    void delayedExileHasSpellSource() {
        Card target = new GrizzlyBears();
        Card encore = new GruesomeEncore();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(encore));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(encore.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Delayed exile remains controlled by the caster after the creature changes control")
    void delayedExileRetainsCasterAfterControlChange() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, creatureId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }
}
