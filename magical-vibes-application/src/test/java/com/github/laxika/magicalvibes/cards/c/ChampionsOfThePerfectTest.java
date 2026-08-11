package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionsOfThePerfectTest extends BaseCardTest {

    @Test
    @DisplayName("Beholds an Elf and returns it to its owner's hand when Champions leaves")
    void beholdsElfAndReturnsItToHand() {
        Card beheldCard = new ElvishMystic();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champions = findPermanent(player1, "Champions of the Perfect");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champions));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Each creature spell cast afterwards draws a card")
    void drawsOnEachCreatureSpell() {
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, elf.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Noncreature spells cast afterwards do not draw")
    void noDrawForNoncreatureSpells() {
        harness.setLibrary(player1, List.of(new AirElemental()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, elf.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
