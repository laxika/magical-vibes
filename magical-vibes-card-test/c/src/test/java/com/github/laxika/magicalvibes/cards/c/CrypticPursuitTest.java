package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TalrandsInvocation;
import com.github.laxika.magicalvibes.cards.t.Thrummingbird;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticPursuit.class, LightningBolt.class, TalrandsInvocation.class, Thrummingbird.class, TimeWarp.class})
class CrypticPursuitTest extends BaseCardTest {

    @Test
    void castingInstantOrSorceryFromHandManifestsTheTopCard() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card topCard = new LightningBolt();
        LightningBolt spell = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void faceDownInstantOrSorceryIsExiledAndMayBeCastUntilNextTurn() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        LightningBolt manifestedCard = new LightningBolt();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent manifested = findPermanent(player1, "Lightning Bolt");
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(manifestedCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, manifestedCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(manifestedCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(manifestedCard);
    }

    @Test
    void castingSorceryFromHandManifestsBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card topCard = new CrypticPursuit();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new TalrandsInvocation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    void castingCreatureFromHandDoesNotManifest() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card topCard = new CrypticPursuit();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Thrummingbird()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isFaceDown);
    }

    @Test
    void opponentsInstantDoesNotManifest() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card topCard = new CrypticPursuit();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isFaceDown);
    }

    @Test
    void emptyLibraryDoesNotPreventTheInstantFromResolving() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isFaceDown);
    }

    @Test
    void faceDownCreatureCardStaysInGraveyard() {
        Card creatureCard = new Thrummingbird();
        Permanent manifested = manifestWithInstant(creatureCard);
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(gd.findExiledCard(creatureCard.getId())).isNull();
    }

    @Test
    void exiledSorceryCanBeCastAndDoesNotManifestAgain() {
        Card sorcery = new TalrandsInvocation();
        Permanent manifested = manifestWithInstant(sorcery);
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        Card topCard = new CrypticPursuit();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castFromExile(player1, sorcery.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isFaceDown);
    }

    @Test
    void permissionExpiresAfterTheControllersNextTurnEvenWhenItIsAnExtraTurn() {
        Card instant = new LightningBolt();
        Permanent manifested = manifestWithInstant(instant);
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(instant.getId())).isNotNull();
    }

    @Test
    void faceUpCreatureDeathDoesNotTriggerExile() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card creatureCard = new Thrummingbird();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, creatureCard);
        creature.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(gd.findExiledCard(creatureCard.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsFaceDownInstantIsNotExiled() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card instant = new LightningBolt();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, instant);
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        creature.setManifested(true);
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(instant);
        assertThat(gd.findExiledCard(instant.getId())).isNull();
    }

    private Permanent manifestWithInstant(Card topCard) {
        harness.addToBattlefield(player1, new CrypticPursuit());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        return findPermanent(player1, topCard.getName());
    }
}
