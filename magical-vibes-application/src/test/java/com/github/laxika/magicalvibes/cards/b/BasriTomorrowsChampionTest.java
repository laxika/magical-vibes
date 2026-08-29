package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasriTomorrowsChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Exerted ability creates a lifelink Cat token")
    void exertedAbilityCreatesCatToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BasriTomorrowsChampion());

        Permanent basri = findPermanent(player1, "Basri, Tomorrow's Champion");
        basri.setSummoningSick(false);
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

        assertThat(ownCat.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ownCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(ownNonCat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ownNonCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponentCat.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentCat.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Basri, Tomorrow's Champion");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
