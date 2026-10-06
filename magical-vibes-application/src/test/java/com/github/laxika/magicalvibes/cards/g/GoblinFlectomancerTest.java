package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CausticRain;
import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.cards.t.TrainOfThought;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFlectomancer.class, CausticRain.class, GodlessShrine.class,
        Pyromatics.class, GhorClanBloodscale.class, Electrolyze.class, Cremate.class, TrainOfThought.class,
        Repeal.class})
class GoblinFlectomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Goblin Flectomancer targets a sorcery spell")
    void activationTargetsSorcerySpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, targetLand.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(causticRain.getId());
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(flectomancer).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Activating Goblin Flectomancer targets an instant spell")
    void activationTargetsInstantSpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, pyromatics.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(pyromatics.getId());
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(flectomancer).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Goblin Flectomancer cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        GhorClanBloodscale ghorClanBloodscale = new GhorClanBloodscale();
        harness.setHand(player1, List.of(ghorClanBloodscale));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, ghorClanBloodscale.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flectomancer);
    }

    @Test
    @DisplayName("Accepting Goblin Flectomancer's may ability changes the spell's target")
    void acceptingRetargetChangesSpellTarget() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget.getId());

        StackEntry causticRainEntry = gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(causticRain.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(causticRainEntry.getTargetId()).isEqualTo(newTarget.getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(newTarget.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }

    @Test
    @DisplayName("Declining Goblin Flectomancer's may ability keeps the original target")
    void decliningRetargetKeepsOriginalTarget() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(originalTarget.getCard().getId()));
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }

    @Test
    @DisplayName("An original target can be reused for another target position when all targets change")
    void canReuseOriginalTargetForDifferentPosition() {
        addCreatureReady(player2, new GoblinFlectomancer());
        Permanent replacement = addCreatureReady(player1, new GhorClanBloodscale());
        addCreatureReady(player1, new GhorClanBloodscale());
        Electrolyze electrolyze = new Electrolyze();
        harness.setHand(player1, List.of(electrolyze));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, electrolyze.getId());
        harness.passBothPriorities();

        StackEntry spell = gd.stack.stream().filter(entry -> entry.getCard().getId().equals(electrolyze.getId()))
                .findFirst().orElseThrow();
        java.util.UUID reusedTarget = spell.getDeclaredTargetIds().getFirst();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, replacement.getId());
        harness.handlePermanentChosen(player2, reusedTarget);

        assertThat(spell.getDamageAssignments())
                .containsExactlyInAnyOrderEntriesOf(Map.of(replacement.getId(), 1, reusedTarget, 1));
        resolveAllTriggers();

        harness.assertLife(player1, reusedTarget.equals(player1.getId()) ? 19 : 20);
        harness.assertLife(player2, reusedTarget.equals(player2.getId()) ? 19 : 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(replacement.getCard());
    }

    @Test
    @DisplayName("Both divided-damage targets change together when legal replacements exist")
    void changesAllDividedDamageTargetsTogether() {
        addCreatureReady(player2, new GoblinFlectomancer());
        Permanent first = addCreatureReady(player1, new GhorClanBloodscale());
        Permanent second = addCreatureReady(player1, new GhorClanBloodscale());
        Electrolyze electrolyze = new Electrolyze();
        harness.setHand(player1, List.of(electrolyze));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Map<java.util.UUID, Integer> originalAssignments = Map.of(player1.getId(), 1, player2.getId(), 1);
        harness.castInstant(player1, 0, originalAssignments);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, electrolyze.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, first.getId());

        StackEntry spell = gd.stack.stream().filter(entry -> entry.getCard().getId().equals(electrolyze.getId()))
                .findFirst().orElseThrow();
        assertThat(spell.getDamageAssignments()).containsExactlyInAnyOrderEntriesOf(originalAssignments);

        harness.handlePermanentChosen(player2, second.getId());
        resolveAllTriggers();
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    @Test
    @DisplayName("A tapped, summoning-sick Goblin Flectomancer can activate its sacrifice ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent flectomancer = harness.addToBattlefieldAndReturn(player2, new GoblinFlectomancer());
        flectomancer.setSummoningSick(true);
        flectomancer.tap();
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, pyromatics.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        harness.assertNotOnBattlefield(player2, "Goblin Flectomancer");
    }

    @Test
    @DisplayName("Goblin Flectomancer can target a spell with no targets")
    void canTargetSpellWithoutTargets() {
        addCreatureReady(player2, new GoblinFlectomancer());
        TrainOfThought trainOfThought = new TrainOfThought();
        GodlessShrine drawnCard = new GodlessShrine();
        harness.setLibrary(player1, List.of(drawnCard, new GodlessShrine()));
        harness.setHand(player1, List.of(trainOfThought));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, trainOfThought.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Train of Thought");
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
    }

    @Test
    @DisplayName("If no other legal target exists the original target is unchanged")
    void keepsOriginalTargetWhenNoLegalReplacementExists() {
        addCreatureReady(player2, new GoblinFlectomancer());
        addCreatureReady(player1, new GhorClanBloodscale());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(originalTarget.getCard());
        harness.assertOnBattlefield(player1, "Ghor-Clan Bloodscale");
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
    }

    @Test
    @DisplayName("Goblin Flectomancer can change a graveyard spell's target to a card in another graveyard")
    void changesGraveyardTargetAcrossGraveyards() {
        addCreatureReady(player2, new GoblinFlectomancer());
        GhorClanBloodscale originalTarget = new GhorClanBloodscale();
        GodlessShrine newTarget = new GodlessShrine();
        GodlessShrine drawnCard = new GodlessShrine();
        harness.setGraveyard(player2, List.of(originalTarget));
        harness.setGraveyard(player1, List.of(newTarget));
        harness.setLibrary(player1, List.of(drawnCard, new GodlessShrine()));
        Cremate cremate = new Cremate();
        harness.setHand(player1, List.of(cremate));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, cremate.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, newTarget.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(newTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(originalTarget);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
    }

    @Test
    @DisplayName("Changing Repeal's target respects the X chosen when it was cast")
    void respectsAnnouncedXWhenChoosingReplacement() {
        addCreatureReady(player2, new GoblinFlectomancer());
        Permanent originalTarget = addCreatureReady(player2, new GhorClanBloodscale());
        Permanent replacement = addCreatureReady(player1, new GhorClanBloodscale());
        Repeal repeal = new Repeal();
        harness.setHand(player1, List.of(repeal));
        harness.addMana(player1, ManaColor.BLUE, 5);
        GodlessShrine drawnCard = new GodlessShrine();
        harness.setLibrary(player1, List.of(drawnCard, new GodlessShrine()));

        harness.castInstant(player1, 0, 4, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, repeal.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, replacement.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(replacement.getCard(), drawnCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(replacement);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
    }
}
