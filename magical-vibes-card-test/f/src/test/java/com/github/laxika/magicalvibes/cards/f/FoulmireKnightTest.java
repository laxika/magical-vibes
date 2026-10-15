package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.g.GarenbrigPaladin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulmireKnight.class, com.github.laxika.magicalvibes.cards.p.ProfaneInsight.class, GarenbrigPaladin.class, DidntSayPlease.class})
class FoulmireKnightTest extends BaseCardTest {

    @Test
    void adventureDrawsCardLosesLifeAndExilesTheCard() {
        FoulmireKnight card = new FoulmireKnight();
        Card drawn = new FoulmireKnight();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        FoulmireKnight card = new FoulmireKnight();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Foulmire Knight");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void creatureCanBeCastDirectlyWithoutDrawingOrLosingLife() {
        Card topCard = new FoulmireKnight();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new FoulmireKnight(), "{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Foulmire Knight");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void adventureCanBeCastDuringOpponentsTurnButCreatureFromExileCannot() {
        FoulmireKnight card = new FoulmireKnight();
        Card drawn = new FoulmireKnight();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Foulmire Knight");
    }

    @Test
    void counteredAdventureDoesNotDrawLoseLifeOrGrantExilePermission() {
        FoulmireKnight card = new FoulmireKnight();
        Card remaining = new FoulmireKnight();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new FoulmireKnight(), new FoulmireKnight(),
                new FoulmireKnight(), remaining));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, card.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        harness.assertLife(player1, 20);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void exileWithoutResolvingAdventureDoesNotPermitCastingCreature() {
        FoulmireKnight card = new FoulmireKnight();
        harness.setExile(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void deathtouchDestroysLargerBlocker() {
        Permanent knight = addCreatureReady(player1, new FoulmireKnight());
        Permanent blocker = addCreatureReady(player2, new GarenbrigPaladin());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(knight)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(knight))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knight);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Foulmire Knight");
        harness.assertInGraveyard(player2, "Garenbrig Paladin");
    }
}