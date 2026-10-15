package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.w.WellWornSpatula;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmaugWickedWorm.class, GiantGrowth.class, SmaugsFury.class, WellWornSpatula.class})
class SmaugWickedWormTest extends BaseCardTest {

    @Test
    void entersWithTappedTreasuresEqualToOpponentsArtifacts() {
        addPermanent(player2, CardType.ARTIFACT, "Artifact one");
        addPermanent(player2, CardType.ARTIFACT, "Artifact two");
        addPermanent(player2, CardType.CREATURE, "Creature");

        SmaugWickedWorm smaug = new SmaugWickedWorm();
        harness.setHand(player1, List.of(smaug));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void drawsAndLosesLifeWhenTreasureManaPaysForSpell() {
        Permanent smaug = addCreatureReady(player1, new SmaugWickedWorm());
        addTreasureToken(player1);
        Card drawnCard = new Card();
        drawnCard.setName("Drawn card");
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new GiantGrowth()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castInstant(player1, 0, smaug.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotTriggerWhenNoTreasureManaPaysForSpell() {
        Permanent smaug = addCreatureReady(player1, new SmaugWickedWorm());
        Card drawnCard = new Card();
        drawnCard.setName("Drawn card");
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, smaug.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void createsNoTreasuresForArtifactsControlledOnlyByItsController() {
        harness.addToBattlefield(player1, new WellWornSpatula());

        harness.enterBattlefieldAndReturn(player1, new SmaugWickedWorm());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsOpponentsArtifactsWhenTheEnterTriggerResolves() {
        harness.addToBattlefield(player2, new WellWornSpatula());
        harness.enterBattlefieldAndReturn(player1, new SmaugWickedWorm());
        harness.addToBattlefield(player2, new WellWornSpatula());
        harness.addToBattlefield(player1, new WellWornSpatula());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void multipleTreasuresPayingForOneSpellTriggerOnlyOneDrawAndLifeLoss() {
        Permanent smaug = addCreatureReady(player1, new SmaugWickedWorm());
        addTreasureToken(player1);
        addTreasureToken(player1);
        Card firstDraw = new WellWornSpatula();
        Card secondDraw = new SmaugsFury();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new SmaugsFury()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.castInstant(player1, 0, smaug.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
    }

    @Test
    void opponentsSpellPaidWithTreasureManaDoesNotTriggerSmaug() {
        Permanent smaug = addCreatureReady(player1, new SmaugWickedWorm());
        addTreasureToken(player2);
        Card drawnCard = new WellWornSpatula();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(new SmaugsFury()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "RED");
        harness.castInstant(player2, 0, smaug.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    private Permanent addPermanent(Player player, CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private Permanent addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));
        return addCreatureReady(player, treasureCard);
    }
}
