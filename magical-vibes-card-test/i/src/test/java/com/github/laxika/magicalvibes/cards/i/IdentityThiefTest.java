package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdentityThief.class, GoblinPiker.class, GoblinKing.class})
class IdentityThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers another nontoken creature as a target")
    void attackOffersMayChoice() {
        addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting exiles the target, copies it, and returns it at the next end step")
    void acceptsExileAndCopy() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblinKing = addCreatureReady(player1, new GoblinKing());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblinKing.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goblinKing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(goblinKing.getOriginalCard());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thief);
        assertThat(gqs.hasKeyword(gd, thief, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == goblinKing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, thief, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, thief)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining leaves the target and Identity Thief unchanged")
    void declinesExile() {
        addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goblin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Token creatures are not legal targets")
    void tokenIsNotLegalTarget() {
        addCreatureReady(player1, new IdentityThief());
        Card tokenCard = new GoblinPiker();
        tokenCard.setToken(true);
        addCreatureReady(player1, tokenCard);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Identity Thief cannot target itself when it is the only creature")
    void cannotTargetItself() {
        addCreatureReady(player1, new IdentityThief());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature is copied and returns under that opponent's control")
    void copiesOpponentsCreature() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(goblin);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(goblin.getOriginalCard());
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(2);

        advanceToEndStep();

        assertThat(findPermanent(player2, "Goblin Piker").getOriginalCard()).isSameAs(goblin.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(2);
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its former controller")
    void stolenCreatureReturnsToOwner() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());
        gd.stolenCreatures.put(goblin.getId(), player2.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(goblin.getOriginalCard());

        advanceToEndStep();

        assertThat(findPermanent(player2, "Goblin Piker").getOriginalCard()).isSameAs(goblin.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(thief);
    }

    @Test
    @DisplayName("The target is exiled and returned even if Identity Thief leaves before resolution")
    void sourceLeavingDoesNotPreventExileOrReturn() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        gd.playerBattlefields.get(player1.getId()).remove(thief);
        gd.playerGraveyards.get(player1.getId()).add(thief.getOriginalCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(goblin.getOriginalCard());

        advanceToEndStep();

        assertThat(findPermanent(player2, "Goblin Piker").getOriginalCard()).isSameAs(goblin.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution is not copied")
    void missingTargetDoesNotChangeThief() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        gd.playerBattlefields.get(player2.getId()).remove(goblin);
        gd.playerGraveyards.get(player2.getId()).add(goblin.getOriginalCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thief)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copying a face-down creature copies its face-down characteristics, not its hidden abilities")
    void copiesFaceDownCharacteristics() {
        Permanent thief = addCreatureReady(player1, new IdentityThief());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());
        Permanent faceDownKing = addCreatureReady(player2, new GoblinKing());
        faceDownKing.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, faceDownKing.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(faceDownKing.getOriginalCard());
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
    }
}
