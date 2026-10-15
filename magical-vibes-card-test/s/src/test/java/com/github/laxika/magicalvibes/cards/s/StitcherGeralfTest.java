package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
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
        RestInPeace.class, Shock.class, Mortivore.class})
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
        Permanent token = findPermanent(player1, "Zombie");
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

    @Test
    @DisplayName("can exile just one of multiple eligible creature cards")
    void canChooseOneCreature() {
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(airElemental, new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(grizzlyBears, new Forest(), new Shock()));
        addAndActivateStitcher();

        harness.handleMultipleCardsChosen(player1, List.of(airElemental.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(airElemental);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(grizzlyBears);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("can choose both creature cards from the same player's graveyard")
    void canChooseTwoCreaturesFromOneGraveyard() {
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(airElemental, grizzlyBears, new Forest()));
        addAndActivateStitcher();

        harness.handleMultipleCardsChosen(player1, List.of(airElemental.getId(), grizzlyBears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(airElemental, grizzlyBears);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("only newly milled creatures are eligible and milling stops after three cards")
    void excludesOldGraveyardCardsAndFourthLibraryCard() {
        Card oldCreature = new AirElemental();
        Card milledCreature = new GrizzlyBears();
        Card fourthCard = new AirElemental();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(milledCreature, new Forest(), new Shock(), fourthCard));
        harness.setLibrary(player2, List.of(new Forest(), new Shock(), new Forest()));
        addAndActivateStitcher();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(milledCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(milledCreature.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCreature);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("short and empty libraries mill what is available without offering noncreatures")
    void handlesShortLibrariesWithoutCreatures() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of());
        addAndActivateStitcher();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("ability-defined power is evaluated in exile after the chosen creature leaves the graveyard")
    void usesCharacteristicDefinedPowerOfExiledCreature() {
        Card mortivore = new Mortivore();
        Card airElemental = new AirElemental();
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(mortivore, new Forest(), new Shock()));
        harness.setLibrary(player2, List.of(airElemental, grizzlyBears, new Forest()));
        addAndActivateStitcher();

        harness.handleMultipleCardsChosen(player1, List.of(mortivore.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mortivore);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(airElemental, grizzlyBears);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("activation pays two generic and one blue mana and taps Geralf before resolving")
    void paysManaAndTapCostsBeforeMilling() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of());
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stitcher),
                0, null, null);

        assertThat(stitcher.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
    private void addAndActivateStitcher() {
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int stitcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(stitcher);
        harness.activateAbility(player1, stitcherIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
