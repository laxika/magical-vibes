package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SterlingGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Other enchantments you control have shroud")
    void otherEnchantmentsYouControlHaveShroud() {
        Permanent grove = new Permanent(new SterlingGrove());
        Permanent otherEnchantment = new Permanent(new GreaterAuramancy());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(grove, otherEnchantment));

        assertThat(gqs.hasKeyword(gd, grove, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherEnchantment, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Sterling Grove searches for an enchantment and puts it on top")
    void sacrificeSearchesForEnchantmentToTop() {
        harness.addToBattlefield(player1, new SterlingGrove());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new GreaterAuramancy(), new GrizzlyBears(), new Plains()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sterling Grove");
        harness.assertInGraveyard(player1, "Sterling Grove");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ENCHANTMENT));

        Card chosen = search.params().cards().getFirst();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
    }
}
