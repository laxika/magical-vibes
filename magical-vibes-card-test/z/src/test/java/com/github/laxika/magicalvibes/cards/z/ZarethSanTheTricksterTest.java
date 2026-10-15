package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ZarethSanTheTrickster.class, ZulaportDuelist.class, CanopyBaloth.class, Forest.class, IntoTheRoil.class})
class ZarethSanTheTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("The hand ability returns an unblocked attacking Rogue and enters tapped and attacking")
    void abilityUsesAnUnblockedRogue() {
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        addCreatureReady(player2, new CanopyBaloth());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new ZarethSanTheTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, rogue.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertInHand(player1, "Zulaport Duelist");
        Permanent zareth = findPermanent(player1, "Zareth San, the Trickster");
        assertThat(zareth.isTapped()).isTrue();
        assertThat(zareth.isAttacking()).isTrue();
        assertThat(zareth.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The hand ability cannot return a non-Rogue attacker")
    void abilityRequiresARogue() {
        Permanent nonRogue = addCreatureReady(player1, new CanopyBaloth());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new ZarethSanTheTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, nonRogue.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage can put any permanent card from the damaged player's graveyard onto the battlefield")
    void combatDamageReanimatesPermanent() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));

        Permanent zareth = addCreatureReady(player1, new ZarethSanTheTrickster());
        zareth.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(forest.getId()));
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("The controller may decline reanimation after choosing the target")
    void mayDeclineReanimationAtResolution() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        dealCombatDamage();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Only permanent cards in the damaged player's graveyard can be targeted")
    void ownGraveyardIsNotOffered() {
        Card ownForest = new Forest();
        Card opponentCreature = new CanopyBaloth();
        Card opponentInstant = new IntoTheRoil();
        harness.setGraveyard(player1, List.of(ownForest));
        harness.setGraveyard(player2, List.of(opponentCreature, opponentInstant));
        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ownForest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Rogue cannot be returned before blockers are declared")
    void cannotActivateBeforeBlockers() {
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        prepareHandAbility();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, rogue.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zulaport Duelist");
        harness.assertInHand(player1, "Zareth San, the Trickster");
    }

    @Test
    @DisplayName("A blocked Rogue cannot pay the return cost")
    void cannotReturnBlockedRogue() {
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        addCreatureReady(player2, new CanopyBaloth());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        prepareHandAbility();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, rogue.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zulaport Duelist");
    }

    @Test
    @DisplayName("Leaving hand in response prevents entry but does not refund the returned Rogue")
    void sourceMustRemainInHand() {
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        prepareHandAbility();
        Card zareth = gd.playerHands.get(player1.getId()).getFirst();

        harness.activateHandAbility(player1, 0, rogue.getId());
        harness.assertInHand(player1, "Zulaport Duelist");
        harness.setHand(player1, List.of(rogue.getCard()));
        harness.setGraveyard(player1, List.of(zareth));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertNotOnBattlefield(player1, "Zareth San, the Trickster");
        harness.assertInGraveyard(player1, "Zareth San, the Trickster");
        harness.assertInHand(player1, "Zulaport Duelist");
    }

    @Test
    @DisplayName("Flash permits casting Zareth San during the opponent's turn")
    void canCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ZarethSanTheTrickster(), "{3}{U}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zareth San, the Trickster");
    }

    @Test
    @DisplayName("A graveyard target that leaves before resolution is not reanimated")
    void targetMustRemainInGraveyard() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(forest));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Combat damage with no permanent card to target does not ask for a choice")
    void noValidGraveyardTarget() {
        harness.setGraveyard(player2, List.of(new IntoTheRoil()));
        dealCombatDamage();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Into the Roil");
        harness.assertNotOnBattlefield(player1, "Into the Roil");
    }

    private void prepareHandAbility() {
        harness.setHand(player1, List.of(new ZarethSanTheTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void dealCombatDamage() {
        addCreatureReady(player1, new ZarethSanTheTrickster()).setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
