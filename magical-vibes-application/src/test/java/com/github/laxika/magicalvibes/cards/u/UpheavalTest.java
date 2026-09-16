package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DivineSacrament;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Upheaval.class, DivineSacrament.class, Forest.class, Persuasion.class, Werebear.class})
class UpheavalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every permanent to its owner's hand")
    void returnsEveryPermanentToItsOwnersHand() {
        Card creature = new Werebear();
        Card land = new Forest();
        Card enchantment = new DivineSacrament();
        Card opponentCreature = new Werebear();

        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player1, land);
        harness.addToBattlefield(player2, enchantment);
        harness.addToBattlefield(player2, opponentCreature);
        harness.setHand(player1, List.of(new Upheaval()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(enchantment, opponentCreature);
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner's hand")
    void returnsStolenPermanentToItsOwnersHand() {
        Card creatureCard = new Werebear();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Persuasion persuasion = new Persuasion();

        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(persuasion));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Upheaval()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(persuasion);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creatureCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
