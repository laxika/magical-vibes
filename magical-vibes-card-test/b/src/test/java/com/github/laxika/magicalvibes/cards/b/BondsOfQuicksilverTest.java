package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GoldenUrn;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Soliton;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondsOfQuicksilver.class, Memnite.class, GoldenUrn.class, Soliton.class})
class BondsOfQuicksilverTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bonds of Quicksilver puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BondsOfQuicksilver.class);
    }

    @Test
    @DisplayName("Resolving Bonds of Quicksilver attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Quicksilver")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Tapped creature with Bonds of Quicksilver does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(bearsPerm.getId());

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other permanents owned by the same player still untap normally")
    void otherPermanentsStillUntap() {
        Permanent enchantedBears = harness.addToBattlefieldAndReturn(player2, new Memnite());
        enchantedBears.setSummoningSick(false);
        enchantedBears.tap();

        Permanent freeBears = harness.addToBattlefieldAndReturn(player2, new Memnite());
        freeBears.setSummoningSick(false);
        freeBears.tap();

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(enchantedBears.getId());

        advanceToNextTurn(player1);

        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature can untap again after Bonds of Quicksilver is removed")
    void creatureUntapsAfterBondsRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bondsPerm);

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Bonds of Quicksilver fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonds of Quicksilver");
        harness.assertNotOnBattlefield(player1, "Bonds of Quicksilver");
    }

    @Test
    @DisplayName("Can target a creature with Bonds of Quicksilver")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Bonds of Quicksilver")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player1, new GoldenUrn());
        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Permanent artifact = findPermanent(player1, "Golden Urn");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Full integration: cast Bonds on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new Memnite());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Quicksilver")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's upkeep without tapping the creature")
    void flashOnOpponentsTurnDoesNotTapCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent bonds = findPermanent(player1, "Bonds of Quicksilver");
        assertThat(bonds.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature can untap through its activated ability")
    void activatedAbilityCanUntapEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Soliton());
        creature.tap();
        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Bonds of Quicksilver");
        creature.tap();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
