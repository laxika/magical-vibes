package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.cards.d.DefyGravity;
import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.cards.m.MirarisWake;
import com.github.laxika.magicalvibes.cards.r.RayOfRevelation;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BenevolentBodyguard.class, CrucibleOfWorlds.class, DefyGravity.class, EmberShot.class, KrosanVerge.class, MentalNote.class, MirarisWake.class, RayOfRevelation.class, ShamansTrance.class})
class ShamansTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Does not itself permit casting Mental Note from an opponent's graveyard")
    void cannotCastMentalNoteWithoutSeparatePermission() {
        MentalNote mentalNote = new MentalNote();
        harness.setGraveyard(player2, List.of(mentalNote));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, mentalNote.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(mentalNote);
    }

    @Test
    @DisplayName("Does not itself permit playing an opponent's graveyard land")
    void cannotPlayLandWithoutSeparatePermission() {
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, krosanVerge.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Krosan Verge");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(krosanVerge);
    }

    @Test
    @DisplayName("Does not prevent its controller from casting from their own graveyard")
    void controllerCanStillCastFromOwnGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player1, new BenevolentBodyguard());
        harness.setGraveyard(player1, List.of(defyGravity));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(defyGravity);
    }

    @Test
    @DisplayName("An opponent regains permission to flash back spells on the next turn")
    void opponentCanFlashBackAgainNextTurn() {
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player2, new BenevolentBodyguard());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(defyGravity));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castFromGraveyardTargeting(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(defyGravity);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Does not itself permit casting Ember Shot from an opponent's graveyard")
    void cannotCastEmberShotWithoutSeparatePermission() {
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player2, List.of(emberShot));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, emberShot.getId(), null, player2.getId(), List.of(), List.of(), null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(emberShot);
    }

    @Test
    @DisplayName("Allows flashing back an opponent's spell and exiles it after resolution")
    void flashesBackOpponentsSpellAndExilesIt() {
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player1, new BenevolentBodyguard());
        harness.setGraveyard(player2, List.of(defyGravity));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playFlashbackSpell(gd, player1, defyGravity.getId(), null, target.getId(), List.of(), List.of(), null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(defyGravity);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(defyGravity);
    }

    @Test
    @DisplayName("Does not advertise an opponent's graveyard land without another permission")
    void doesNotAdvertiseLandWithoutSeparatePermission() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService()
                .canPlayGraveyardLand(gd, player1.getId(), krosanVerge, player2.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Permission to flash back an opponent's spell expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player1, new BenevolentBodyguard());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(defyGravity));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, defyGravity.getId(), null, target.getId(), List.of(), List.of(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(defyGravity);
    }

    @Test
    @DisplayName("Prevents an opponent from using flashback this turn")
    void preventsOtherPlayersFromCastingSpellsFromTheirGraveyards() {
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player2, new BenevolentBodyguard());
        harness.setGraveyard(player2, List.of(defyGravity));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(defyGravity);
    }

    @Test
    @DisplayName("Prevents other players from playing lands from their graveyards")
    void preventsOtherPlayersFromPlayingLandsFromTheirGraveyards() {
        harness.addToBattlefield(player2, new CrucibleOfWorlds());
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, krosanVerge.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(krosanVerge);
        harness.assertNotOnBattlefield(player2, "Krosan Verge");
    }

    @Test
    @DisplayName("Does not allow its controller to play cards from their own graveyard")
    void doesNotAllowControllerToPlayCardsFromTheirOwnGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player1, List.of(emberShot));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, emberShot.getId(), null, player2.getId(), List.of(), List.of(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(emberShot);
    }

    @Test
    @DisplayName("Uses the opponent's spell's flashback cost instead of its mana cost")
    void usesFlashbackCostForOpponentsSpell() {
        RayOfRevelation ray = new RayOfRevelation();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirarisWake());
        harness.setGraveyard(player2, List.of(ray));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playFlashbackSpell(gd, player1, ray.getId(), null, target.getId(), List.of(), List.of(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mirari's Wake");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(ray);
    }

    @Test
    @DisplayName("Crucible of Worlds allows playing an opponent's land while Shaman's Trance applies")
    void playsOpponentsLandWithCrucibleAndRespectsLandLimit() {
        KrosanVerge first = new KrosanVerge();
        KrosanVerge second = new KrosanVerge();
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player2, List.of(first, second));
        prepareMainPhase(player1);
        harness.castFromHand(player1, new ShamansTrance(), "{2}{R}");
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, first.getId());

        harness.assertOnBattlefield(player1, "Krosan Verge");
        assertThat(findPermanent(player1, "Krosan Verge").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
    }

}
