package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
import com.github.laxika.magicalvibes.cards.i.InfernoTitan;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlalekFusedAtrocity.class, EldraziDevastator.class,
        LeylineOfAnticipation.class, ProdigalPyromancer.class, InfernoTitan.class})
class UlalekFusedAtrocityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Eldrazi spell offers to pay {C}{C}")
    void eldrazispellOffersCopyPayment() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the payment does not copy the Eldrazi spell")
    void decliningPaymentDoesNotCopy() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Eldrazi Devastator");
    }

    @Test
    @DisplayName("Paying copies the current spell and activated ability")
    void payingCopiesCurrentSpellAndActivatedAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        int pyromancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer);
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Eldrazi Devastator"));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Prodigal Pyromancer"));

        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Copied abilities are above all copied spells even when the original ability is below a spell")
    void copiesSpellsBeforeAbilities() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer),
                null, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(4);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
    }

    @Test
    @DisplayName("A copied creature spell becomes a token and does not trigger Ulalek again")
    void creatureSpellCopyBecomesTokenWithoutCastTrigger() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Devastator")).hasSize(2);
        assertThat(findPermanents(player1, "Eldrazi Devastator"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Colored mana cannot pay the two colorless mana for Ulalek")
    void paymentRequiresColorlessMana() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Eldrazi Devastator")).hasSize(1);
    }

    @Test
    @DisplayName("Casting a non-Eldrazi does not trigger Ulalek")
    void nonEldraziDoesNotTrigger() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.setHand(player1, List.of(new ProdigalPyromancer()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Prodigal Pyromancer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Eldrazi spell does not trigger Ulalek")
    void opponentEldraziDoesNotTrigger() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player2, new LeylineOfAnticipation());
        harness.setHand(player2, List.of(new EldraziDevastator()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Eldrazi Devastator");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting Ulalek itself does not trigger its own ability")
    void castingUlalekDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new UlalekFusedAtrocity()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ulalek, Fused Atrocity");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ulalek copies another pending Ulalek trigger but excludes its resolving trigger")
    void copiesOtherTriggeredAbilities() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.setHand(player1, List.of(new EldraziDevastator(), new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 18);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(6);
        assertThat(gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(2);
        assertThat(gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL))
                .hasSize(4);
    }

    @Test
    @DisplayName("Ulalek does not copy an opponent's activated ability")
    void opponentAbilityIsNotCopied() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY))
                .hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A copied activated ability can target a different permanent")
    void copiedActivatedAbilityCanChooseNewTarget() {
        Permanent ulalek = harness.addToBattlefieldAndReturn(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer),
                null, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ulalek.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(ulalek.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A copied triggered ability with multiple targets offers new targets")
    void multiTargetTriggeredAbilityCanChooseNewTargets() {
        harness.addToBattlefield(player1, new UlalekFusedAtrocity());
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new InfernoTitan(), new EldraziDevastator()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 14);
        gd.pendingETBDamageAssignments = Map.of(pyromancer.getId(), 1, player2.getId(), 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
