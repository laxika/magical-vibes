package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlameRift;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SyrCarahTheBold.class, FlameRift.class, GrizzlyBears.class, Shock.class, Mountain.class})
class SyrCarahTheBoldTest extends BaseCardTest {

    @Test
    @DisplayName("Its activated ability exiles the top card after damaging a player")
    void activatedAbilityTriggersTheExile() {
        Permanent syrCarah = addReadySyrCarah();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(syrCarah.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controlled instant dealing damage to a player exiles the top card")
    void instantDamageToPlayerTriggersTheExile() {
        addReadySyrCarah();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Damage to a creature by an instant does not trigger the exile")
    void instantDamageToCreatureDoesNotTriggerTheExile() {
        addReadySyrCarah();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("A spell damaging two players creates one trigger for each player")
    void spellDamageToTwoPlayersTriggersTwice() {
        addReadySyrCarah();
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new FlameRift()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (UUID) null);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
    }

    private Permanent addReadySyrCarah() {
        return addCreatureReady(player1, new SyrCarahTheBold());
    }

    @Test
    void combatDamageExilesOneCardRegardlessOfDamageAmount() {
        addReadySyrCarah();
        Card topCard = new Mountain();
        Card nextCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void activatedAbilityCanDamageItsControllerAndTrigger() {
        addReadySyrCarah();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void activatedAbilityDamagesCreatureWithoutExiling() {
        addReadySyrCarah();
        Permanent target = addCreatureReady(player2, new SyrCarahTheBold());
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void opponentSpellDamageDoesNotTrigger() {
        addReadySyrCarah();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void emptyLibraryDoesNotPreventDamageOrCauseALoss() {
        addReadySyrCarah();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void exiledInstantRequiresManaAndCanBeCastAfterSyrCarahLeaves() {
        Permanent syrCarah = addReadySyrCarah();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(syrCarah);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void exiledLandCanBePlayedDuringMainPhase() {
        addReadySyrCarah();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void permissionExpiresAtEndOfTurnButCardRemainsExiled() {
        addReadySyrCarah();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
