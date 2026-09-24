package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StitcherGeralf.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        RestInPeace.class, Shock.class})
class StitcherGeralfTest extends BaseCardTest {

    @Test
    @DisplayName("mills each player and creates a Zombie with the total power of chosen cards")
    void millsAndCreatesTokenWithTotalPower() {
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(airElemental, new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(grizzlyBears, new Forest(), new Shock()));
        addAndActivateStitcher();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(airElemental.getId(), grizzlyBears.getId());
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(airElemental.getId(), grizzlyBears.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(airElemental);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(grizzlyBears);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Zombie");
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("can decline all creature cards and the resulting 0/0 token dies")
    void canDeclineCreatureCards() {
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(airElemental, new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(grizzlyBears, new Forest(), new Shock()));
        addAndActivateStitcher();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(airElemental);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(grizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("creature cards replaced into exile are not eligible")
    void replacementIntoExileRemovesCardsFromChoice() {
        harness.addToBattlefield(player1, new RestInPeace());
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(airElemental, new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(grizzlyBears, new Forest(), new Shock()));
        addAndActivateStitcher();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(airElemental);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(grizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addAndActivateStitcher() {
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int stitcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(stitcher);
        harness.activateAbility(player1, stitcherIndex, 0, null, null);
        harness.passBothPriorities();
        return stitcher;
    }
}
