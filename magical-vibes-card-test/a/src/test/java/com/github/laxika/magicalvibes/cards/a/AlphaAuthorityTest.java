package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PitFight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaAuthority.class, AdaptiveSnapjaw.class, PitFight.class})
class AlphaAuthorityTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaAuthority());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AdaptiveSnapjaw());
        blocker.setSummoningSick(false);
        return blocker;
    }

    private void beginBlocks() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    @DisplayName("Enchanted creature has hexproof")
    void enchantedCreatureHasHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();

        enchant(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Creature loses hexproof when Alpha Authority leaves")
    void hexproofLostWhenAuraRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        Permanent aura = enchant(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a single creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addAttacker();
        enchant(attacker);
        addBlocker();

        beginBlocks();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addAttacker();
        enchant(attacker);
        addBlocker();
        addBlocker();

        beginBlocks();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Unenchanted creature can still be blocked by two creatures")
    void unenchantedCreatureCanBeMultiBlocked() {
        addAttacker();
        addBlocker();
        addBlocker();

        beginBlocks();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Aura resolves attached to the targeted creature")
    void auraResolvesAttachedToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        harness.setHand(player1, List.of(new AlphaAuthority()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AlphaAuthority)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the enchanted creature")
    void opponentCannotTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        enchant(creature);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSnapjaw());
        harness.setHand(player2, List.of(new PitFight()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                List.of(opponentCreature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof allows the creature's controller to target it")
    void controllerCanTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        enchant(creature);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSnapjaw());
        harness.setHand(player1, List.of(new PitFight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0,
                List.of(creature.getId(), opponentCreature.getId()));

        harness.assertInGraveyard(player1, "Adaptive Snapjaw");
        harness.assertInGraveyard(player2, "Adaptive Snapjaw");
    }

    @Test
    @DisplayName("Blocking restriction ends when the Aura leaves")
    void multiBlockingAllowedAfterAuraLeaves() {
        Permanent attacker = addAttacker();
        Permanent aura = enchant(attacker);
        addBlocker();
        addBlocker();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        beginBlocks();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isBlocking);
    }
}
