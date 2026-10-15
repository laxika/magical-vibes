package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightNurseHealerOfHeroes.class, Disenchant.class, MindRot.class, TormodsCrypt.class})
class NightNurseHealerOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a permanent put into the graveyard from the battlefield this turn")
    void returnsPermanentPutIntoGraveyardThisTurn() {
        TormodsCrypt crypt = new TormodsCrypt();
        var cryptPermanent = harness.addToBattlefieldAndReturn(player1, crypt);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, cryptPermanent.getId());

        harness.setHand(player1, List.of(new NightNurseHealerOfHeroes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(crypt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(crypt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
        harness.assertNotInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("ETB can target a permanent discarded from hand this turn")
    void returnsPermanentPutIntoGraveyardFromHandThisTurn() {
        TormodsCrypt crypt = new TormodsCrypt();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(crypt, new Disenchant()));
        harness.setHand(player2, List.of(new MindRot()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player1, List.of(new NightNurseHealerOfHeroes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(crypt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(crypt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("ETB cannot target an old permanent card")
    void cannotTargetOldPermanentCard() {
        harness.setGraveyard(player1, List.of(new TormodsCrypt()));
        castNightNurse();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("ETB cannot target a nonpermanent card")
    void cannotTargetNonpermanentCard() {
        harness.setGraveyard(player1, List.of(new Disenchant()));
        castNightNurse();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Disenchant");
    }

    @Test
    @DisplayName("ETB only offers the recent permanent when an older permanent is also in the graveyard")
    void excludesOldPermanentAlongsideRecentPermanent() {
        TormodsCrypt oldCrypt = new TormodsCrypt();
        TormodsCrypt recentCrypt = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(oldCrypt));
        var permanent = harness.addToBattlefieldAndReturn(player1, recentCrypt);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        castNightNurse();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(recentCrypt.getId());
        harness.handleMultipleCardsChosen(player1, List.of(recentCrypt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCrypt).doesNotContain(recentCrypt);
        assertThat(gd.playerHands.get(player1.getId())).contains(recentCrypt).doesNotContain(oldCrypt);
    }

    @Test
    @DisplayName("ETB cannot target a permanent put into an opponent's graveyard this turn")
    void cannotTargetOpponentsRecentPermanent() {
        TormodsCrypt crypt = new TormodsCrypt();
        var permanent = harness.addToBattlefieldAndReturn(player2, crypt);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        castNightNurse();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Tormod's Crypt");
        harness.assertNotInHand(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("ETB does not return a target exiled in response")
    void doesNotReturnTargetExiledInResponse() {
        TormodsCrypt target = new TormodsCrypt();
        var permanent = harness.addToBattlefieldAndReturn(player1, target);
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        castNightNurse();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getId()));
        harness.assertNotInHand(player1, "Tormod's Crypt");
        harness.assertNotInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Lifelink gains life from unblocked combat damage")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NightNurseHealerOfHeroes());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castNightNurse() {
        harness.setHand(player1, List.of(new NightNurseHealerOfHeroes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
