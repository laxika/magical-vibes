package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necrosynthesis.class, GrizzlyBears.class, HillGiant.class, Forest.class, Mountain.class,
        Island.class})
class NecrosynthesisTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets a +1/+1 counter when another creature dies")
    void putsCounterOnEnchantedCreatureWhenAnotherCreatureDies() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(enchanted);

        other.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
    }

    @Test
    @DisplayName("When enchanted creature dies, look at cards equal to its power and keep one")
    void looksAtCardsEqualToEnchantedCreaturePower() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachAura(enchanted);
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card island = new Island();
        setDeck(player1, List.of(forest, mountain, island));

        enchanted.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, mountain, island);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new Necrosynthesis());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
