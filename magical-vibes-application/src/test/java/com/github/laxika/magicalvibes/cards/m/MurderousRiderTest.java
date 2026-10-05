package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ClaimTheFirstborn;
import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.d.Dreadbore;
import com.github.laxika.magicalvibes.cards.e.EpicDownfall;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwiftEnd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurderousRider.class, SwiftEnd.class, Dreadbore.class, GarrukWildspeaker.class,
        GrizzlyBears.class, Plains.class, DidntSayPlease.class, ClaimTheFirstborn.class, EpicDownfall.class})
class MurderousRiderTest extends BaseCardTest {

    @Test
    void adventureDestroysCreatureLosesLifeAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MurderousRider card = new MurderousRider();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureDestroysPlaneswalker() {
        Permanent target = addReadyPlaneswalker(player2, 3);
        MurderousRider card = new MurderousRider();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void adventureRejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new MurderousRider()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MurderousRider card = new MurderousRider();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Murderous Rider");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void whenItDiesItIsPutOnTheBottomOfItsOwnersLibrary() {
        MurderousRider card = new MurderousRider();
        harness.addToBattlefield(player1, card);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent rider = findPermanent(player1, "Murderous Rider");
        harness.castSorcery(player1, 0, rider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, card);
    }

    @Test
    void creatureFaceCanBeCastDirectlyFromHandWithoutLosingLife() {
        harness.setHand(player1, List.of(new MurderousRider()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Murderous Rider");
        harness.assertLife(player1, 20);
    }

    @Test
    void lifelinkGainsLifeWhenTheCreatureDealsCombatDamage() {
        Permanent rider = addCreatureReady(player1, new MurderousRider());
        rider.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void adventureWithAnIllegalTargetDoesNotLoseLifeOrGrantExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MurderousRider());
        MurderousRider card = new MurderousRider();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new MurderousRider()));
        harness.setLife(player1, 20);
        for (Player player : List.of(player1, player2)) {
            harness.addMana(player, ManaColor.BLACK, 2);
            harness.addMana(player, ManaColor.COLORLESS, 1);
        }

        harness.castAdventure(player1, 0, target.getId());
        harness.castAdventure(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void counteredAdventureGoesToGraveyardWithoutLosingLifeOrTriggeringDeath() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MurderousRider());
        MurderousRider card = new MurderousRider();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.castInstant(player2, 0, card.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void deathUnderAnotherPlayersControlReturnsItToItsOwnersLibrary() {
        MurderousRider card = new MurderousRider();
        Permanent rider = harness.addToBattlefieldAndReturn(player2, card);
        Card libraryCard = new Plains();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new ClaimTheFirstborn(), new MurderousRider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, rider.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rider);
        harness.castAdventure(player1, 0, rider.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
    }

    @Test
    void deathTriggerPutsTheCardIntoAnEmptyLibrary() {
        MurderousRider card = new MurderousRider();
        Permanent rider = harness.addToBattlefieldAndReturn(player2, card);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new MurderousRider()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, rider.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
    }

    @Test
    void exilingTheCreatureDoesNotTriggerItsDeathAbilityOrGrantCastingPermission() {
        MurderousRider card = new MurderousRider();
        Permanent rider = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new EpicDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, rider.getId());

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
