package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BismuthMindrender.class, Forest.class, GrizzlyBears.class})
class BismuthMindrenderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles through lands and offers a cast during resolution")
    void combatDamageDigsToNonlandAndOffersCastDuringResolution() {
        addAttackingMindrender();
        Card land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(land.getId(), nonland.getId());
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land, nonland);
    }

    @Test
    @DisplayName("Exiling only lands grants no cast permission")
    void onlyLandsDoNotGrantPermission() {
        addAttackingMindrender();
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the cast leaves the card exiled without permission to cast later")
    void decliningCastDoesNotGrantPermissionForLater() {
        addAttackingMindrender();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty damaged player's library offers no cast")
    void emptyLibraryOffersNoCast() {
        addAttackingMindrender();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class, AngelicChorus.class})
    @DisplayName("A split card is cast as the chosen half, paying life equal to only that half's mana value")
    void splitCardPaysLifeForTheChosenHalf() {
        addAttackingMindrender();
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID chorusId = harness.getPermanentId(player2, "Angelic Chorus");
        WearTear wearTear = new WearTear();
        harness.setLibrary(player2, List.of(wearTear));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Wear — Destroy target artifact", "Tear — Destroy target enchantment");
        harness.handleListChoice(player1, "Tear — Destroy target enchantment");
        assertThat(gd.findExiledCard(wearTear.getId())).isNotNull();
        harness.handlePermanentChosen(player1, chorusId);

        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tear");
        assertThat(gd.findExiledCard(wearTear.getId())).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(wearTear.getId()));
    }

    private Permanent addAttackingMindrender() {
        Permanent mindrender = addCreatureReady(player1, new BismuthMindrender());
        mindrender.setAttacking(true);
        return mindrender;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
