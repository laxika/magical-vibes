package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({QuintoriusKand.class, Forest.class, GrizzlyBears.class, Shock.class})
class QuintoriusKandTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a 3/2 red and white Spirit")
    void plusOneCreatesSpirit() {
        addReadyQuintorius(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 discovers 4")
    void minusThreeDiscoversFour() {
        addReadyQuintorius(player1, 5);
        Card discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    @Test
    @DisplayName("Casting a spell from exile deals damage to opponents and gains life")
    void castingFromExileTriggersDamageAndLife() {
        addReadyQuintorius(player1, 5);
        Card exiled = new GrizzlyBears();
        harness.setExile(player1, List.of(exiled));
        gd.exilePlayPermissions.put(exiled.getId(), player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("-6 exiles any number of your graveyard cards, adds red mana, and grants play permission")
    void minusSixExilesYourGraveyardCards() {
        addReadyQuintorius(player1, 6);
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCard));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    void discoveredSpellTriggersDamageAndLife() {
        addReadyQuintorius(player1, 5);
        Card discovered = new GrizzlyBears();
        Card skippedLand = new Forest();
        Card skippedExpensiveSpell = new QuintoriusKand();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(skippedLand, skippedExpensiveSpell, discovered, remaining));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                remaining, skippedLand, skippedExpensiveSpell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningDiscoveredSpellPutsItInHandWithoutTriggering() {
        addReadyQuintorius(player1, 5);
        Card discovered = new GrizzlyBears();
        Card land = new Forest();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(land, discovered, remaining));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void discoverWithoutQualifyingCardReturnsEverythingToLibrary() {
        addReadyQuintorius(player1, 5);
        Card land = new Forest();
        Card expensive = new QuintoriusKand();
        harness.setLibrary(player1, List.of(land, expensive));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void castingFromHandDoesNotTriggerDamageAndLife() {
        addReadyQuintorius(player1, 5);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsExileCastDoesNotTriggerDamageAndLife() {
        addReadyQuintorius(player1, 5);
        Card spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castFromExile(player2, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void minusSixCanChooseNoCards() {
        Permanent quintorius = addReadyQuintorius(player1, 7);
        Card card = new Shock();
        harness.setGraveyard(player1, List.of(card));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(quintorius.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void minusSixCountsOnlyTargetsStillInGraveyard() {
        addReadyQuintorius(player1, 7);
        Card missing = new GrizzlyBears();
        Card remaining = new Shock();
        harness.setGraveyard(player1, List.of(missing, remaining));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2,
                List.of(missing.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(missing));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(missing, remaining);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.exilePlayPermissions).containsEntry(remaining.getId(), player1.getId())
                .doesNotContainKey(missing.getId());
    }

    @Test
    void minusSixAllowsPaidSpellCastAndTriggersWhileQuintoriusRemains() {
        addReadyQuintorius(player1, 7);
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void minusSixAllowsLandPlayWithoutTriggering() {
        addReadyQuintorius(player1, 7);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(land.getId()));
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void minusSixPermissionExpiresAtEndOfTurn() {
        addReadyQuintorius(player1, 7);
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
    }

    @Test
    void createdSpiritHasBothColorsAndSpiritSubtype() {
        Permanent quintorius = addReadyQuintorius(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(gqs.hasEffectiveSubtype(gd, spirit, CardSubtype.SPIRIT)).isTrue();
        assertThat(quintorius.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    private Permanent addReadyQuintorius(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new QuintoriusKand());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
