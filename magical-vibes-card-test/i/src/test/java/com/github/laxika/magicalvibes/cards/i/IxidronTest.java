package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AcademyRuins;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.h.HerdGnarr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ixidron.class, AshcoatBear.class, AcademyRuins.class, HerdGnarr.class, DressDown.class})
class IxidronTest extends BaseCardTest {

    @Test
    void turnsOtherNontokenCreaturesFaceDownAndCountsThem() {
        Permanent ownCreature = addCreatureReady(player1, new AshcoatBear());
        Permanent opposingCreature = addCreatureReady(player2, new AshcoatBear());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(ownCreature.isFaceDown()).isTrue();
        assertThat(opposingCreature.isFaceDown()).isTrue();
        assertThat(ixidron.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(2);
    }

    @Test
    void excludesCreatureTokensFromTurningFaceDownAndFromItsCount() {
        Permanent nontokenCreature = addCreatureReady(player1, new AshcoatBear());
        Card tokenCard = new AshcoatBear();
        tokenCard.setToken(true);
        Permanent tokenCreature = addCreatureReady(player1, tokenCard);

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(nontokenCreature.isFaceDown()).isTrue();
        assertThat(tokenCreature.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    @Test
    void countsExistingFaceDownCreaturesLeavesNoncreaturesFaceUpAndUsesTwoTwoCharacteristics() {
        Permanent existingFaceDownCreature = addCreatureReady(player1, new AshcoatBear());
        existingFaceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new AcademyRuins());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(existingFaceDownCreature.isFaceDown()).isTrue();
        assertThat(creature.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(noncreature.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(2);
    }

    @Test
    void updatesItsPowerAndToughnessWhenFaceDownCreaturesLeave() {
        Permanent removedCreature = addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removedCreature));

        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    @Test
    void diesWhenThereAreNoFaceDownCreatures() {
        castIxidron();

        assertThat(countPermanents(player1, "Ixidron")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Ixidron);
    }

    @Test
    void secondIxidronTurnsFirstIxidronFaceDownAndCountsIt() {
        addCreatureReady(player2, new AshcoatBear());
        castIxidron();
        Permanent firstIxidron = findPermanent(player1, "Ixidron");

        castIxidron();

        Permanent secondIxidron = findPermanents(player1, "Ixidron").getLast();
        assertThat(firstIxidron.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstIxidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstIxidron)).isEqualTo(2);
        assertThat(secondIxidron.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondIxidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondIxidron)).isEqualTo(2);
    }

    @Test
    void preservesTappedStatusAndCountersWhenTurningCreaturesFaceDown() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castIxidron();

        assertThat(creature.isFaceDown()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void countsFaceDownTokensButDoesNotCountFaceDownNoncreatures() {
        Card tokenCard = new AshcoatBear();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player2, tokenCard);
        token.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AcademyRuins());
        land.setFaceDown(0, 0, Set.of(CardType.LAND));

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(token.isFaceDown()).isTrue();
        assertThat(land.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    @Test
    @CardUsed(HerdGnarr.class)
    void turnsCreaturesFaceDownBeforeTheyCanTriggerFromItsEntry() {
        Permanent gnarr = addCreatureReady(player1, new HerdGnarr());

        castIxidron();

        assertThat(gnarr.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, gnarr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarr)).isEqualTo(2);
    }

    @Test
    @CardUsed(DressDown.class)
    void doesNotTurnCreaturesFaceDownWhenDressDownRemovesItsAbilities() {
        harness.addToBattlefield(player2, new DressDown());
        Permanent creature = addCreatureReady(player1, new AshcoatBear());

        castIxidron();

        assertThat(creature.isFaceDown()).isFalse();
        assertThat(countPermanents(player1, "Ixidron")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Ixidron);
    }

    private void castIxidron() {
        harness.castFromHand(player1, new Ixidron(), "{3}{U}{U}");
        harness.passBothPriorities();
    }
}
