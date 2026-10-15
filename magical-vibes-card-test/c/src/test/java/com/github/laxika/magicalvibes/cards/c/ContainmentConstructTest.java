package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SeasonOfRenewal;
import com.github.laxika.magicalvibes.cards.t.ThirstForKnowledge;
import com.github.laxika.magicalvibes.cards.v.VirusBeetle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContainmentConstruct.class, RavensCrime.class, Shock.class, Mountain.class,
        SeasonOfRenewal.class, ThirstForKnowledge.class, VirusBeetle.class})
class ContainmentConstructTest extends BaseCardTest {

    @Test
    void acceptingDiscardTriggerExilesCardAndAllowsCastingItThisTurn() {
        harness.addToBattlefield(player1, new ContainmentConstruct());
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(discarded);
        assertThat(gd.exilePlayPermissions).containsEntry(discarded.getId(), player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromExile(player1, discarded.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void decliningDiscardTriggerLeavesCardInGraveyard() {
        harness.addToBattlefield(player1, new ContainmentConstruct());
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    @Test
    void discardedLandsCanBePlayedButDoNotGrantAdditionalLandPlays() {
        Mountain first = new Mountain();
        Mountain second = new Mountain();
        harness.addToBattlefield(player1, new ContainmentConstruct());
        harness.setHand(player1, List.of(new ThirstForKnowledge(), first, second));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThatThrownBy(() -> harness.castFromExile(player2, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionRespectsCreatureTimingAndExpiresBeforeTheNextTurn() {
        VirusBeetle discarded = new VirusBeetle();
        harness.addToBattlefield(player1, new ContainmentConstruct());
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new VirusBeetle()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    @Test
    void originalDiscardTriggerCannotExileCardThatLeftAndReenteredGraveyard() {
        VirusBeetle discarded = new VirusBeetle();
        harness.addToBattlefield(player1, new ContainmentConstruct());
        harness.setHand(player1, List.of(discarded, new SeasonOfRenewal(), new ThirstForKnowledge()));
        harness.setHand(player2, List.of(new VirusBeetle()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(discarded.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(discarded);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }
}
