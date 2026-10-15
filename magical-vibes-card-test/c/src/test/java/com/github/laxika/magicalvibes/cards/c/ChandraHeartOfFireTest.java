package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.cards.v.VolcanicSalvo;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.ThrillOfPossibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraHeartOfFire.class, Forest.class, Opt.class, Shock.class, ThrillOfPossibility.class, OnakkeOgre.class, VolcanicSalvo.class})
class ChandraHeartOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("+1 discards the hand and grants permission to play the top three cards")
    void plusOneDiscardsHandAndExilesTopThreeForPlay() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Opt();
        Card discardedOne = new Forest();
        Card discardedTwo = new Opt();
        harness.setHand(player1, List.of(discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("+1 deals 2 damage to a target player")
    void plusOneDealsTwoDamage() {
        Permanent chandra = addReadyChandra(player1, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("−9 exiles chosen red spells from the graveyard and library and adds six red mana")
    void minusNineExilesChosenRedSpellsAndAddsMana() {
        Permanent chandra = addReadyChandra(player1, 9);
        Card graveyardRed = new Shock();
        Card graveyardBlue = new Opt();
        Card libraryRed = new Shock();
        Card libraryBlue = new Opt();
        harness.setGraveyard(player1, List.of(graveyardRed, graveyardBlue));
        harness.setLibrary(player1, List.of(libraryRed, libraryBlue));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardRed.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(graveyardRed.getId(), libraryRed.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(graveyardRed.getId(), player1.getId())
                .containsEntry(libraryRed.getId(), player1.getId())
                .doesNotContainKey(graveyardBlue.getId())
                .doesNotContainKey(libraryBlue.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardBlue)
                .doesNotContain(graveyardRed, libraryRed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryBlue);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    void emptyHandStillExilesCardsAndAllowsLandPlayWithinNormalLimit() {
        addReadyChandra(player1, 5);
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        harness.castFromExile(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).contains(first.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        addReadyChandra(player1, 5);
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }

    @Test
    void ultimateCanChooseNoCardsFromEitherZoneAndStillAddsMana() {
        addReadyChandra(player1, 9);
        Card graveyardSpell = new Shock();
        Card librarySpell = new Shock();
        harness.setGraveyard(player1, List.of(graveyardSpell));
        harness.setLibrary(player1, List.of(librarySpell));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(librarySpell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    void ultimateCanExileMultipleLibraryCardsAndCastThemAfterChandraDies() {
        addReadyChandra(player1, 9);
        Card first = new Shock();
        Card second = new Shock();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Chandra, Heart of Fire");
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, second.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void playPermissionExpiresButUnplayedCardsRemainExiled() {
        addReadyChandra(player1, 5);
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }

    @Test
    void ultimateAllowsCastingSpellWithPayableAdditionalDiscardCost() {
        addReadyChandra(player1, 9);
        Card thrill = new ThrillOfPossibility();
        Card discard = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.setGraveyard(player1, List.of(thrill));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(thrill.getId()));

        assertThatCode(() -> harness.castFromExile(player1, thrill.getId()))
                .doesNotThrowAnyException();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(thrill);
    }

    @Test
    void damageAbilityCanTargetCreature() {
        addReadyChandra(player1, 5);
        Card ogre = new OnakkeOgre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, ogre);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(ogre);
        harness.assertNotOnBattlefield(player2, "Onakke Ogre");
    }

    @Test
    void damageAbilityCanTargetPlaneswalker() {
        addReadyChandra(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        target.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void ultimateIncludesRedSorceriesButExcludesRedNonSpellsAndLands() {
        addReadyChandra(player1, 9);
        Card sorcery = new VolcanicSalvo();
        Card creature = new OnakkeOgre();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(sorcery, creature, land));
        harness.setLibrary(player1, List.of(new ChandraHeartOfFire(), new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraHeartOfFire());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
