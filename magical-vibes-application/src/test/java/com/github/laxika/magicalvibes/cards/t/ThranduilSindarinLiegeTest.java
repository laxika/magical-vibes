package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilvanRally;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranduilSindarinLiege.class, SilvanRally.class, ElvishMystic.class, Forest.class,
        GrizzlyBears.class})
class ThranduilSindarinLiegeTest extends BaseCardTest {

    @Test
    void boostsOtherElvesButNotNonElvesOrItself() {
        harness.addToBattlefield(player1, new ThranduilSindarinLiege());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingElf)).isEqualTo(1);
    }

    @Test
    void landfallCreatesAnElfToken() {
        harness.addToBattlefield(player1, new ThranduilSindarinLiege());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ELF))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void adventureMillsFourAndReturnsUpToTwoMilledLands() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        List<Card> library =
                List.of(firstLand, new GrizzlyBears(), secondLand, new GrizzlyBears());
        harness.setLibrary(player1, library);
        ThranduilSindarinLiege card = new ThranduilSindarinLiege();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureReturnsOnlyTwoOfFourMilledLands() {
        Forest oldLand = new Forest();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setGraveyard(player1, List.of(oldLand));
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        ThranduilSindarinLiege card = new ThranduilSindarinLiege();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldLand, first, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanDeclineAllLandsFromAShortLibraryAndThenCastCreature() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        ThranduilSindarinLiege card = new ThranduilSindarinLiege();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thranduil, Sindarin Liege");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void opposingLandEntryDoesNotCreateAToken() {
        harness.addToBattlefield(player1, new ThranduilSindarinLiege());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
