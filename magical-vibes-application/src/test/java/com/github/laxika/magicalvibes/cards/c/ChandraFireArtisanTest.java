package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraFireArtisan.class, Forest.class, ChandrasPyrohelix.class})
class ChandraFireArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("+1 exiles the top card and grants permission to play it this turn")
    void plusOneExilesTopCardWithPlayPermission() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top, new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 exiles seven cards and deals seven damage to the chosen opponent")
    void ultimateExilesSevenAndTriggersDamage() {
        addReadyChandra(player1, 7);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    @Test
    void exiledLandCanBePlayed() {
        addReadyChandra(player1, 4);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledSpellStillRequiresItsManaCost() {
        addReadyChandra(player1, 4);
        Card spell = new ChandraFireArtisan();
        harness.setLibrary(player1, List.of(spell));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void unplayedCardStaysExiledAfterPlayPermissionExpires() {
        addReadyChandra(player1, 4);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card, new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void plusOneWithEmptyLibraryDoesNotDealDamage() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateWithShortLibraryGrantsPermissionForEveryRemainingCard() {
        addReadyChandra(player1, 8);
        List<Card> cards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, cards);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(cards);
        for (Card card : cards) {
            assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
        }
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void lethalDamageTriggersForOnlyTheCountersActuallyRemoved() {
        Permanent chandra = addReadyChandra(player1, 1);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, Map.of(chandra.getId(), 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra, Fire Artisan");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void removalTriggerCanTargetChandraHerselfAndTriggerAgain() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, Map.of(chandra.getId(), 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra, Fire Artisan");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraFireArtisan());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
