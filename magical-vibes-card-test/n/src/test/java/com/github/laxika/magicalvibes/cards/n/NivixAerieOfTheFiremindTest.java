package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.c.CullingSun;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.SkarrganPitSkulk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivixAerieOfTheFiremind.class, Quicken.class, CullingSun.class, SkarrganPitSkulk.class})
class NivixAerieOfTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent nivix = addReadyNivix();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(nivix.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles and lets its controller cast a top instant until the next turn")
    void castsTopInstantFromExile() {
        Card quicken = activateExileAbility(new Quicken());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(quicken);
        assertThat(gd.exilePlayPermissions).containsEntry(quicken.getId(), player1.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, quicken.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(quicken);
    }

    @Test
    @DisplayName("Lets its controller cast a top sorcery from exile")
    void castsTopSorceryFromExile() {
        harness.addToBattlefield(player2, new SkarrganPitSkulk());
        Card cullingSun = activateExileAbility(new CullingSun());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, cullingSun.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Skarrgan Pit-Skulk");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cullingSun);
    }

    @Test
    @DisplayName("Exiles but does not allow casting a non-instant or non-sorcery")
    void doesNotAllowCastingCreatureFromExile() {
        Card creature = activateExileAbility(new SkarrganPitSkulk());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Keeps the casting permission through the opponent's turn, not into its controller's next turn")
    void castingPermissionExpiresWhenNextTurnBegins() {
        Card quicken = activateExileAbility(new Quicken());
        harness.setLibrary(player1, List.of(new SkarrganPitSkulk(), new SkarrganPitSkulk()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(quicken.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(quicken.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, quicken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An exiled adventurer card receives permission to cast its sorcery Adventure")
    @CardUsed({BeanstalkGiant.class, FertileFootsteps.class})
    void permitsCastingSorceryAdventure() {
        Card giant = activateExileAbility(new BeanstalkGiant());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(giant);
        assertThat(harness.getCastingPermissionService()
                .hasExilePlayPermission(gd, player1.getId(), giant.getId())).isTrue();
    }

    @Test
    @DisplayName("The casting permission does not waive the spell's mana cost")
    void requiresManaToCastExiledInstant() {
        Card quicken = activateExileAbility(new Quicken());

        assertThatThrownBy(() -> harness.castFromExile(player1, quicken.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(quicken);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An exiled sorcery cannot be cast during the opponent's turn")
    void sorceryStillRequiresNormalTiming() {
        Card cullingSun = activateExileAbility(new CullingSun());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, cullingSun.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cullingSun);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent cannot use the controller's casting permission")
    void onlyControllerCanCastExiledCard() {
        Card quicken = activateExileAbility(new Quicken());
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, quicken.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(quicken);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activation or cause a failed draw")
    void emptyLibraryExilesNothing() {
        Permanent nivix = addReadyNivix();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(nivix.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The ability exiles the top card at resolution, even if Nivix has left")
    void exilesCurrentTopCardAfterSourceLeaves() {
        Permanent nivix = addReadyNivix();
        Card originalTop = new Quicken();
        Card currentTop = new CullingSun();
        harness.setLibrary(player1, List.of(originalTop, currentTop));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.setLibrary(player1, List.of(currentTop, originalTop));
        gd.playerBattlefields.get(player1.getId()).remove(nivix);
        gd.playerGraveyards.get(player1.getId()).add(nivix.getCard());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(currentTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        assertThat(gd.exilePlayPermissions).containsEntry(currentTop.getId(), player1.getId());
    }

    @Test
    @DisplayName("Insufficient activation mana leaves Nivix untapped and the library untouched")
    void requiresFullActivationCost() {
        Permanent nivix = addReadyNivix();
        Card topCard = new Quicken();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nivix.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyNivix() {
        return addCreatureReady(player1, new NivixAerieOfTheFiremind());
    }

    private Card activateExileAbility(Card topCard) {
        Permanent nivix = addReadyNivix();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int nivixIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nivix);
        harness.activateAbility(player1, nivixIndex, 1, null, null);
        harness.passBothPriorities();
        return topCard;
    }
}
