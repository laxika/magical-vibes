package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dehydration.class, GrizzlyBears.class, Island.class, DrakeHatchling.class, CreditVoucher.class})
class DehydrationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dehydration puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        Dehydration dehydration = new Dehydration();

        harness.setHand(player1, List.of(dehydration));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isSameAs(dehydration);
    }

    @Test
    @DisplayName("Resolving Dehydration attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        Dehydration dehydration = new Dehydration();

        harness.setHand(player1, List.of(dehydration));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bearsPerm.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == dehydration
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Cannot cast Dehydration without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tapped creature with Dehydration does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        bearsPerm.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());

        advanceToUpkeep(player2);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped creature with Dehydration remains untapped (Dehydration does not tap)")
    void untappedCreatureRemainsUntapped() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());

        advanceToUpkeep(player2);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other permanents owned by the same player still untap normally")
    void otherPermanentsStillUntap() {
        Permanent enchantedBears = addCreatureReady(player2, new GrizzlyBears());
        enchantedBears.tap();

        Permanent freeBears = addCreatureReady(player2, new GrizzlyBears());
        freeBears.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(enchantedBears.getId());

        advanceToUpkeep(player2);

        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature stays tapped across multiple turns")
    void creatureStaysTappedAcrossMultipleTurns() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        bearsPerm.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());

        advanceToUpkeep(player2);
        assertThat(bearsPerm.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(bearsPerm.isTapped()).isTrue();

        advanceToUpkeep(player2);
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple Dehydrations on different creatures prevent both from untapping")
    void multipleDehydrationsOnDifferentCreatures() {
        Permanent bears1 = addCreatureReady(player2, new GrizzlyBears());
        bears1.tap();

        Permanent bears2 = addCreatureReady(player2, new GrizzlyBears());
        bears2.tap();

        Permanent dehydration1 = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydration1.setAttachedTo(bears1.getId());

        Permanent dehydration2 = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydration2.setAttachedTo(bears2.getId());

        advanceToUpkeep(player2);

        assertThat(bears1.isTapped()).isTrue();
        assertThat(bears2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature can untap again after Dehydration is removed")
    void creatureUntapsAfterDehydrationRemoved() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        bearsPerm.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(dehydrationPerm);

        advanceToUpkeep(player2);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dehydration can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Dehydration dehydration = new Dehydration();

        harness.setHand(player1, List.of(dehydration));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == dehydration
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Dehydration on own creature prevents it from untapping")
    void dehydrationOnOwnCreaturePreventsUntap() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());

        advanceToUpkeep(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dehydration fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        Dehydration dehydration = new Dehydration();

        harness.setHand(player1, List.of(dehydration));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearsPerm));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dehydration);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == dehydration);
    }

    @Test
    @DisplayName("Can target a creature with Dehydration")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dehydration")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Full integration: cast Dehydration on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        bearsPerm.tap();
        Dehydration dehydration = new Dehydration();

        harness.setHand(player1, List.of(dehydration));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == dehydration
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));

        advanceToUpkeep(player2);

        assertThat(bearsPerm.isTapped()).isTrue();
    }
}
