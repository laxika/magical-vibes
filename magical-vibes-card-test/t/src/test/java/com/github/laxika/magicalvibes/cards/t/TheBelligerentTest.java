package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BitterTriumph;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TheBelligerent.class, AirElemental.class, BitterTriumph.class, Forest.class, Shock.class})
class TheBelligerentTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure and grants top-library play until end of turn")
    void attackingCreatesTreasureAndGrantsTopLibraryPlay() {
        Shock shock = new Shock();
        attackWithBelligerent(shock);

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        assertThat(gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn).contains(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The attack trigger permits playing a land from the top of the library")
    void attackTriggerPermitsPlayingLandFromTop() {
        Forest forest = new Forest();
        attackWithBelligerent(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Top-library permission is private and expires at cleanup")
    void topLibraryPermissionIsPrivateAndExpires() {
        Shock shock = new Shock();
        attackWithBelligerent(shock);

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn).doesNotContain(player1.getId());
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The permission follows the changing top card and allows multiple spells")
    void canCastSuccessiveTopCards() {
        attackWithBelligerent(new Shock());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 11);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Top-library permission does not grant an additional land play")
    void cannotPlayASecondLand() {
        attackWithBelligerent(new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Top-library permission preserves land timing")
    void cannotPlayLandDuringCombat() {
        attackWithBelligerent(new Forest());
        harness.forceStep(TurnStep.END_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Top-library permission preserves creature spell timing")
    void cannotCastCreatureDuringCombat() {
        attackWithBelligerent(new AirElemental());
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castAndResolveFromLibraryTop(player1);
        assertThat(countPermanents(player1, "Air Elemental")).isEqualTo(2);
    }

    @Test
    @DisplayName("Top-library spells still require their mana cost")
    void cannotCastWithoutEnoughMana() {
        attackWithBelligerent(new AirElemental());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Air Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick creature can pay Crew 3")
    void summoningSickCreatureCanCrew() {
        addCreatureReady(player1, new TheBelligerent());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        crew.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat(player1);

        harness.assertLife(player2, 15);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell from the library may pay its mandatory additional life cost")
    void canPayAdditionalLifeCostFromLibraryTop() {
        attackWithBelligerent(new BitterTriumph());
        Permanent target = findPermanent(player1, "Air Elemental");
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveFromLibraryTop(player1, target.getId());

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Bitter Triumph");
    }

    @Test
    @DisplayName("The granted permission survives The Belligerent leaving the battlefield")
    void permissionSurvivesSourceRemoval() {
        attackWithBelligerent(new Shock());
        Permanent belligerent = findPermanent(player1, "The Belligerent");
        harness.setHand(player1, List.of(new BitterTriumph()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, belligerent.getId());

        harness.assertInGraveyard(player1, "The Belligerent");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("An empty library does not prevent the attack trigger from making a Treasure")
    void emptyLibraryStillCreatesTreasure() {
        addCreatureReady(player1, new TheBelligerent());
        addCreatureReady(player1, new AirElemental());
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger does not permit the opponent to cast from their library")
    void opponentDoesNotReceivePermission() {
        attackWithBelligerent(new Forest());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Crew 3 cannot be activated without untapped creatures to pay its cost")
    void cannotCrewWithoutCreatures() {
        addCreatureReady(player1, new TheBelligerent());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void attackWithBelligerent(Card topCard) {
        addCreatureReady(player1, new TheBelligerent());
        addCreatureReady(player1, new AirElemental());
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat(player1);
    }
}
