package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaskingRootwalla.class, RavensCrime.class})
class BaskingRootwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Basking Rootwalla +2/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent rootwalla = addReadyRootwalla(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump ability can be activated only once each turn")
    void pumpAbilityOnlyOncePerTurn() {
        addReadyRootwalla(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("The pump wears off at end of turn cleanup")
    void pumpWearsOffAtEndOfTurn() {
        Permanent rootwalla = addReadyRootwalla(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(1);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding Basking Rootwalla offers its zero-cost madness cast")
    void discardTriggersMadness() {
        BaskingRootwalla rootwalla = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rootwalla.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting zero-cost madness puts Basking Rootwalla onto the battlefield")
    void acceptingMadnessCastsForNoMana() {
        BaskingRootwalla rootwalla = discardViaRavensCrime();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(rootwalla.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addReadyRootwalla(Player player) {
        Permanent permanent = new Permanent(new BaskingRootwalla());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private BaskingRootwalla discardViaRavensCrime() {
        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(rootwalla));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return rootwalla;
    }
}
