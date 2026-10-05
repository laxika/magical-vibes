package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuintoriusLoremaster.class, Shock.class, GrizzlyBears.class, Forest.class})
class QuintoriusLoremasterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target noncreature nonland card and creates a 3/2 Spirit")
    void exilesTargetAndCreatesSpirit() {
        Permanent quintorius = addReadyQuintorius();
        Card shock = new Shock();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land, shock));

        advanceToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(shock.getId())
                && entry.sourcePermanentId().equals(quintorius.getId()));
        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getName()).isEqualTo("Spirit");
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardColor.RED,
                com.github.laxika.magicalvibes.model.CardColor.WHITE);
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getEffectivePower()).isEqualTo(3);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants a targeted free cast that puts the spell on the bottom of its owner's library")
    void grantsTargetedFreeCastWithBottomReplacement() {
        Permanent quintorius = addReadyQuintorius();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, shock.getId(), Zone.EXILE);
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of());
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(quintorius.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not create a Spirit when there is no legal graveyard target")
    void noSpiritWithoutLegalTarget() {
        addReadyQuintorius();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new Shock()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not create a Spirit when the graveyard target leaves before resolution")
    void noSpiritWhenTargetLeavesGraveyard() {
        addReadyQuintorius();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(shock));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        addReadyQuintorius();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects an exiled card not exiled with Quintorius before paying costs")
    void rejectsUnlinkedExileTarget() {
        Permanent quintorius = addReadyQuintorius();
        Card linked = new Shock();
        harness.setGraveyard(player1, List.of(linked));
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(linked.getId()));
        harness.passBothPriorities();
        Card unrelated = new Shock();
        harness.setExile(player1, List.of(unrelated));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, unrelated.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quintorius.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting permission survives Quintorius leaving before the ability resolves")
    void castingPermissionSurvivesSourceLeaving() {
        Permanent quintorius = addReadyQuintorius();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, shock.getId(), Zone.EXILE);
        gd.playerBattlefields.get(player1.getId()).remove(quintorius);
        harness.setGraveyard(player1, List.of(quintorius.getCard()));
        harness.passBothPriorities();

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Declining to cast leaves the card exiled and permission expires at end of turn")
    void unusedCastingPermissionExpires() {
        addReadyQuintorius();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, shock.getId(), Zone.EXILE);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyQuintorius() {
        return addCreatureReady(player1, new QuintoriusLoremaster());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
