package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.g.Gargadon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitchenImp.class, RavensCrime.class, Gargadon.class})
class KitchenImpTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding Kitchen Imp offers its madness cost")
    void discardTriggersMadness() {
        KitchenImp imp = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(imp.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness puts Kitchen Imp into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        KitchenImp imp = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(imp.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(imp.getId()));
    }

    @Test
    @DisplayName("Accepting madness casts Kitchen Imp for {B}")
    void acceptingMadnessCastsCreature() {
        KitchenImp imp = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(imp.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Kitchen Imp can be cast normally and attack immediately past a ground creature")
    void normalCastHasHasteAndFlying() {
        KitchenImp imp = new KitchenImp();
        addCreatureReady(player2, new Gargadon());
        harness.castFromHand(player1, imp, "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(imp.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kitchen Imp goes to the graveyard when its madness cost cannot be paid")
    void unpaidMadnessGoesToGraveyard() {
        KitchenImp imp = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(imp.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(imp.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(imp.getId()));
    }

    private KitchenImp discardViaRavensCrime() {
        KitchenImp imp = new KitchenImp();
        harness.setHand(player1, List.of(imp));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return imp;
    }
}
