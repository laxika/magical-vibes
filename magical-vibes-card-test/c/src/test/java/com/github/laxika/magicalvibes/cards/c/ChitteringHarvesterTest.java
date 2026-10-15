package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringHarvester.class, AlmightyBrushwagg.class, BenalishKnight.class, Unsummon.class,
        CacklingCounterpart.class, Murder.class, SoulWarden.class})
class ChitteringHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating makes each opponent sacrifice a creature")
    void mutatingMakesEachOpponentSacrificeCreature() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Each opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        Permanent first = addCreatureReady(player2, new AlmightyBrushwagg());
        Permanent second = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Casting normally does not cause an opponent to sacrifice")
    void normalCastDoesNotTriggerSacrifice() {
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new ChitteringHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chittering Harvester");
        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertNotInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Mutate can be cast for four generic and one black mana")
    void mutateCostMergesAndTriggersSacrifice() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new ChitteringHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TOP");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("An opponent without creatures has nothing to sacrifice")
    void opponentWithoutCreaturesDoesNotPrompt() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());

        triggerMutation(harvester);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chittering Harvester");
    }

    @Test
    @DisplayName("The sacrifice trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        gd.playerBattlefields.get(player1.getId()).remove(harvester);
        gd.playerGraveyards.get(player1.getId()).add(harvester.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Each subsequent mutation causes another sacrifice")
    void subsequentMutationTriggersAgain() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");

        addCreatureReady(player2, new AlmightyBrushwagg());
        triggerMutation(harvester);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Almighty Brushwagg"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Mutating on top keeps the permanent's counters, tapped state, and underlying abilities")
    void mutatingOnTopPreservesPermanentStateAndAbilities() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        host.tap();
        host.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareMutate(host, "TOP");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(host);
        assertThat(host.isTapped()).isTrue();
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, host, Keyword.TRAMPLE)).isTrue();
        activateInheritedBoost(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(9);
    }

    @Test
    @DisplayName("Mutating on bottom retains the host's characteristics and gains the mutate ability")
    void mutatingOnBottomRetainsHostAndTriggers() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());
        prepareMutate(host, "BOTTOM");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        assertThat(host.getTimesMutated()).isEqualTo(1);
    }

    @Test
    @DisplayName("Merging does not trigger battlefield-entry abilities")
    void mutationDoesNotTriggerCreatureEnteringBattlefield() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player1, new SoulWarden());
        prepareMutate(host, "TOP");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A mutate spell whose target leaves resolves as a normal creature without a mutate trigger")
    void departedTargetMakesMutateResolveNormally() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());
        setUpMutate();
        harness.castWithAlternateCost(player1, 0, host.getId());
        gd.playerBattlefields.get(player1.getId()).remove(host);
        gd.playerGraveyards.get(player1.getId()).add(host.getOriginalCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chittering Harvester");
        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mutate cannot target a Human or a creature owned by another player")
    void mutateRejectsHumanAndWrongOwner() {
        Permanent human = addCreatureReady(player1, new BenalishKnight());
        Permanent opponentCreature = addCreatureReady(player2, new AlmightyBrushwagg());
        setUpMutate();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, human.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returning a merged creature to hand returns both physical cards")
    void returningMergedCreatureReturnsAllCards() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        prepareMutate(host, "TOP");
        bounce(host);

        harness.assertNotOnBattlefield(player1, "Chittering Harvester");
        harness.assertInHand(player1, "Almighty Brushwagg");
        harness.assertInHand(player1, "Chittering Harvester");
    }

    @Test
    @DisplayName("A token on top stays a token while a returned underlying card goes to hand")
    void returningTokenTopStillReturnsUnderlyingCard() {
        Card token = new AlmightyBrushwagg();
        token.setToken(true);
        Permanent host = addCreatureReady(player1, token);
        prepareMutate(host, "BOTTOM");
        assertThat(gqs.isToken(gd, host)).isTrue();
        bounce(host);

        harness.assertInHand(player1, "Chittering Harvester");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A token copy copies all merged abilities but is one physical object")
    void copyingMergedCreatureCopiesAbilitiesWithoutComponents() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        prepareMutate(host, "TOP");
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(host.getId())).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.TRAMPLE)).isTrue();
        activateInheritedBoost(copy);
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(copy.isMergedByMutation()).isFalse();
        assertThat(copy.cardsLeavingBattlefield()).hasSize(1);
    }

    @Test
    @DisplayName("The owner orders all physical cards in the graveyard even when another player controls the merged creature")
    void ownerOrdersMergedCardsInGraveyard() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        gd.playerBattlefields.get(player1.getId()).remove(host);
        gd.playerBattlefields.get(player2.getId()).add(host);
        gd.stolenCreatures.put(host.getId(), player1.getId());
        prepareMutate(host, "TOP");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, host.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).extracting(Card::getName)
                .containsExactly("Chittering Harvester", "Almighty Brushwagg");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsSubsequence("Almighty Brushwagg", "Chittering Harvester");
        harness.assertNotInGraveyard(player2, "Chittering Harvester");
    }

    private void setUpMutate() {
        harness.setHand(player1, List.of(new ChitteringHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void prepareMutate(Permanent host, String position) {
        setUpMutate();
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, position);
    }

    private void activateInheritedBoost(Permanent permanent) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent),
                0, null, null);
        harness.passBothPriorities();
    }

    private void bounce(Permanent host) {
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, host.getId());
        harness.passBothPriorities();
    }

    private void triggerMutation(Permanent harvester) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, harvester, List.of(harvester.getCard()), player1.getId()));
    }
}
