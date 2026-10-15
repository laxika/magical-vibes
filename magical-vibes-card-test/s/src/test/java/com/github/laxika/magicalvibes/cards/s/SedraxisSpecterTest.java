package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Sedraxis Specter")
@CardUsed({SedraxisSpecter.class, KathariScreecher.class, Forest.class, ResoundingThunder.class})
class SedraxisSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card")
    void combatDamageMakesDamagedPlayerDiscard() {
        Permanent specter = addCreatureReady(player1, new SedraxisSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new KathariScreecher(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No discard when the Specter is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        Permanent specter = addCreatureReady(player1, new SedraxisSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KathariScreecher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Unearth returns Sedraxis Specter to the battlefield with haste")
    void unearthReturnsWithHaste() {
        SedraxisSpecter card = new SedraxisSpecter();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Sedraxis Specter");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Sedraxis Specter");
    }

    @Test
    @DisplayName("Unearthed Sedraxis Specter is exiled at the next end step")
    void unearthExiledAtEndStep() {
        SedraxisSpecter card = new SedraxisSpecter();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sedraxis Specter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sedraxis Specter"));
    }

    @Test
    void combatDamageAgainstEmptyHandStillResolves() {
        Permanent specter = addCreatureReady(player1, new SedraxisSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new SedraxisSpecter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sedraxis Specter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new SedraxisSpecter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sedraxis Specter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new SedraxisSpecter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sedraxis Specter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalDamageExilesUnearthedSpecterInsteadOfPuttingItInGraveyard() {
        SedraxisSpecter card = new SedraxisSpecter();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Sedraxis Specter").getId());

        harness.assertNotOnBattlefield(player1, "Sedraxis Specter");
        harness.assertNotInGraveyard(player1, "Sedraxis Specter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }
}
