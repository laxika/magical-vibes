package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasriTomorrowsChampion.class, BrimazKingOfOreskos.class, GrizzlyBears.class})
class BasriTomorrowsChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Exerted ability creates a lifelink Cat token")
    void exertedAbilityCreatesCatToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent basri = addCreatureReady(player1, new BasriTomorrowsChampion());
        harness.addMana(player1, ManaColor.WHITE, 1);

        int basriIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basri);
        harness.activateAbility(player1, basriIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(cat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(cat.getCard().getSubtypes()).contains(CardSubtype.CAT);
        assertThat(cat.getCard().getPower()).isEqualTo(1);
        assertThat(cat.getCard().getToughness()).isEqualTo(1);
        assertThat(cat.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(basri.isTapped()).isTrue();
        assertThat(basri.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Cycling grants your Cats hexproof and indestructible and draws")
    void cyclingProtectsCatsAndDraws() {
        harness.setHand(player1, List.of(new BasriTomorrowsChampion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent ownCat = addCreatureReady(player1, new BrimazKingOfOreskos());
        Permanent ownNonCat = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCat = addCreatureReady(player2, new BrimazKingOfOreskos());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCat.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ownCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(ownNonCat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ownNonCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponentCat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Basri, Tomorrow's Champion");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exert is paid before the token ability resolves")
    void exertIsPaidOnActivation() {
        Permanent basri = addCreatureReady(player1, new BasriTomorrowsChampion());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(basri.isTapped()).isTrue();
        assertThat(basri.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(basri);
    }

    @Test
    @DisplayName("Cycling protection resolves separately before drawing the card")
    void cyclingProtectionResolvesBeforeDraw() {
        harness.setHand(player1, List.of(new BasriTomorrowsChampion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent cat = addCreatureReady(player1, new BrimazKingOfOreskos());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Basri, Tomorrow's Champion");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(cat.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(cat.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotInHand(player1, "Grizzly Bears");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exert skips only the controller's next untap step")
    void exertSkipsOneUntapStep() {
        Permanent basri = addCreatureReady(player1, new BasriTomorrowsChampion());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(basri.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(basri.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(basri.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cycling protection expires at end of turn")
    void cyclingProtectionExpires() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BasriTomorrowsChampion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent cat = addCreatureReady(player1, new BrimazKingOfOreskos());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(cat.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(cat.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(cat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(cat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cycling without Cats still draws and does not protect later Cats")
    void cyclingWithoutCatsDoesNotProtectLaterCats() {
        harness.setHand(player1, List.of(new BasriTomorrowsChampion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent laterCat = addCreatureReady(player1, new BrimazKingOfOreskos());
        assertThat(laterCat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(laterCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
