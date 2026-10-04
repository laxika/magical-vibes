package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AllWillBeOne;
import com.github.laxika.magicalvibes.cards.b.BlueSunsTwilight;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.h.HaltOrder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MishraArtificerProdigy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EncroachingMycosynth.class, AllWillBeOne.class, Island.class,
        CrawlingChorus.class, BlueSunsTwilight.class})
class EncroachingMycosynthTest extends BaseCardTest {

    @Test
    void ownNonlandPermanentsBecomeArtifacts() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new AllWillBeOne());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        assertThat(gqs.isArtifact(gd, ownPermanent)).isTrue();
        assertThat(gqs.isArtifact(gd, ownLand)).isFalse();
        assertThat(gqs.isArtifact(gd, opponentPermanent)).isFalse();
    }

    @Test
    void ownNonlandPermanentCardsBecomeArtifactsOutsideBattlefield() {
        Card ownCard = new AllWillBeOne();
        Card ownLand = new Island();
        Card opponentCard = new CrawlingChorus();
        harness.setHand(player1, List.of(ownCard, ownLand));
        harness.setHand(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        assertThat(gqs.cardHasType(ownCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(ownLand, CardType.ARTIFACT, gd, player1.getId())).isFalse();
        assertThat(gqs.cardHasType(opponentCard, CardType.ARTIFACT, gd, player2.getId())).isFalse();
    }

    @Test
    void cardsInGraveyardLibraryAndExileGainArtifactTypeWithoutLosingOtherTypes() {
        Card graveyardCard = new CrawlingChorus();
        Card libraryCard = new AllWillBeOne();
        Card exiledCard = new CrawlingChorus();
        Card land = new Island();
        Card sorcery = new BlueSunsTwilight();
        harness.setGraveyard(player1, List.of(graveyardCard, sorcery));
        harness.setLibrary(player1, List.of(libraryCard, land));
        harness.setExile(player1, List.of(exiledCard));
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        assertThat(gqs.cardHasType(graveyardCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(graveyardCard, CardType.CREATURE, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(libraryCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(libraryCard, CardType.ENCHANTMENT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(exiledCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(land, CardType.ARTIFACT, gd, player1.getId())).isFalse();
        assertThat(gqs.cardHasType(sorcery, CardType.ARTIFACT, gd, player1.getId())).isFalse();
    }

    @Test
    void permanentSpellIsAnArtifactInAdditionToItsOtherTypes() {
        Card creature = new CrawlingChorus();
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gqs.cardHasType(creature, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(creature, CardType.CREATURE, gd, player1.getId())).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, gqs.findPermanentById(gd, creature.getId()))).isTrue();
    }

    @Test
    void removingMycosynthEndsTheGrantOnPermanentsAndCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        Card handCard = new AllWillBeOne();
        harness.setHand(player1, List.of(handCard));
        Permanent mycosynth = harness.addToBattlefieldAndReturn(player1, new EncroachingMycosynth());
        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        assertThat(gqs.cardHasType(handCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mycosynth));

        assertThat(gqs.isArtifact(gd, creature)).isFalse();
        assertThat(gqs.cardHasType(handCard, CardType.ARTIFACT, gd, player1.getId())).isFalse();
    }

    @Test
    @CardUsed(MishraArtificerProdigy.class)
    void grantedArtifactSpellTriggersMishra() {
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new CrawlingChorus(), new CrawlingChorus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @CardUsed(HaltOrder.class)
    void grantedArtifactSpellCanBeCounteredByHaltOrder() {
        Card creature = new CrawlingChorus();
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.setHand(player1, List.of(creature));
        harness.setHand(player2, List.of(new HaltOrder()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Crawling Chorus");
        harness.assertNotOnBattlefield(player1, "Crawling Chorus");
        harness.assertInHand(player2, "Island");
    }
}
