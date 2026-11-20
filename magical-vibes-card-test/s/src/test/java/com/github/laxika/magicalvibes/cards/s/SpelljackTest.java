package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirecatBlitz;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirecatBlitz.class, HaplessResearcher.class, MentalNote.class, Spelljack.class, SuntailHawk.class})
class SpelljackTest extends BaseCardTest {

    @Test
    @DisplayName("Counters and exiles the target spell, granting its own controller a free play while it stays exiled")
    void countersAndExilesTargetSpellWithFreeCastPermissionForItsController() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, hawk, "{W}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hawk.getId()));
        assertThat(gd.exilePlayPermissionHolder(hawk.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).contains(hawk.getId());
        harness.assertNotInGraveyard(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Spelljack's controller can cast the countered spell from exile without paying mana")
    void controllerCanCastCounteredSpellFromExileWithoutMana() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, hawk, "{W}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.findExiledCard(hawk.getId())).isNull();
    }

    @Test
    @DisplayName("Can't target a permanent on the battlefield")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target an activated ability")
    void cannotTargetActivatedAbility() {
        harness.addToBattlefield(player1, new HaplessResearcher());
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        var ability = gd.stack.getLast();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ability.getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A countered X spell is cast from exile with X equal to 0")
    void castsCounteredXSpellWithXEqualToZero() {
        FirecatBlitz firecatBlitz = new FirecatBlitz();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player1, List.of(firecatBlitz));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, firecatBlitz.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, firecatBlitz.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Firecat Blitz");
        assertThat(gd.findExiledCard(firecatBlitz.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Elemental Cat"));
    }

    @Test
    @DisplayName("Counters and exiles a noncreature spell")
    void countersAndExilesNoncreatureSpell() {
        MentalNote mentalNote = new MentalNote();
        Spelljack spelljack = new Spelljack();
        harness.castFromHand(player1, mentalNote, "{U}");
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mentalNote.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(mentalNote.getId()));
        assertThat(gd.exilePlayPermissionHolder(mentalNote.getId())).isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Mental Note");
    }

    @Test
    @DisplayName("The exiled creature keeps its timing, and a refused cast keeps the permission")
    void creatureCannotBeCastOnOpponentsTurnAndFailedAttemptPreservesPermission() {
        SuntailHawk hawk = new SuntailHawk();
        harness.castFromHand(player1, hawk, "{W}");
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThatThrownBy(() -> harness.castFromExile(player2, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(hawk.getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("The countered card's owner can't use Spelljack's permission")
    void ownerCannotUseSpelljacksPermission() {
        SuntailHawk hawk = new SuntailHawk();
        harness.castFromHand(player1, hawk, "{W}");
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(hawk.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("A stolen instant can be cast on an opponent's turn and goes to its owner's graveyard")
    void stolenInstantCanBeCastOnOpponentsTurnAndResolvesToOwnersGraveyard() {
        MentalNote mentalNote = new MentalNote();
        harness.castFromHand(player1, mentalNote, "{U}");
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLibrary(player2, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mentalNote.getId());

        harness.castFromExile(player2, mentalNote.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mental Note");
        harness.assertNotInGraveyard(player2, "Mental Note");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.findExiledCard(mentalNote.getId())).isNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(mentalNote.getId());
        assertThat(gd.exilePlayWithoutPayingCardIds()).doesNotContain(mentalNote.getId());
    }

    @Test
    @DisplayName("A spell cast with flashback is countered into exile and can be cast again")
    void spellCastWithFlashbackCanBeCounteredAndCastAgainNormally() {
        FirecatBlitz firecatBlitz = new FirecatBlitz();
        harness.setGraveyard(player1, List.of(firecatBlitz));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFlashback(player1, 0, 0, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, firecatBlitz.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, firecatBlitz.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Firecat Blitz");
        assertThat(gd.findExiledCard(firecatBlitz.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
