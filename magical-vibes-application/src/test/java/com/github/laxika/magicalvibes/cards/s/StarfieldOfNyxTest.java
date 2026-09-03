package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Browse;
import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfieldOfNyx.class, SylvanEchoes.class, GossamerChains.class, Insight.class,
        Browse.class, GrizzlyBears.class, Pacifism.class})
class StarfieldOfNyxTest extends BaseCardTest {

    /** Starfield plus four more enchantments — the "five or more enchantments" threshold. */
    private void addFiveEnchantments() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        harness.addToBattlefield(player1, new SylvanEchoes());
        harness.addToBattlefield(player1, new GossamerChains());
        harness.addToBattlefield(player1, new Insight());
        harness.addToBattlefield(player1, new Browse());
    }

    @Test
    @DisplayName("Upkeep trigger offers an enchantment card in the graveyard as a target")
    void upkeepOffersGraveyardEnchantment() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        harness.setGraveyard(player1, List.of(new Insight()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Chosen enchantment card returns from the graveyard to the battlefield")
    void returnsChosenEnchantmentToBattlefield() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Insight");
        harness.assertNotInGraveyard(player1, "Insight");
    }

    @Test
    @DisplayName("Nonenchantment cards in the graveyard are not legal targets")
    void nonEnchantmentIsNotATarget() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With five enchantments, other non-Aura enchantments are creatures with P/T equal to mana value")
    void animatesOtherEnchantmentsAtFive() {
        addFiveEnchantments();

        Permanent echoes = findPermanent(player1, "Sylvan Echoes");
        Permanent browse = findPermanent(player1, "Browse");

        assertThat(gqs.isCreature(gd, echoes)).isTrue();
        assertThat(gqs.getEffectivePower(gd, echoes)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, echoes)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, browse)).isTrue();
        assertThat(gqs.getEffectivePower(gd, browse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, browse)).isEqualTo(4);
    }

    @Test
    @DisplayName("Starfield of Nyx does not animate itself")
    void doesNotAnimateItself() {
        addFiveEnchantments();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Starfield of Nyx"))).isFalse();
    }

    @Test
    @DisplayName("With only four enchantments nothing is animated")
    void doesNotAnimateBelowThreshold() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        harness.addToBattlefield(player1, new SylvanEchoes());
        harness.addToBattlefield(player1, new GossamerChains());
        harness.addToBattlefield(player1, new Insight());

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Sylvan Echoes"))).isFalse();
    }

    @Test
    @DisplayName("Auras are not animated")
    void doesNotAnimateAuras() {
        addFiveEnchantments();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Pacifism());
        Permanent pacifism = findPermanent(player1, "Pacifism");
        pacifism.setAttachedTo(bears.getId());

        assertThat(gqs.isCreature(gd, pacifism)).isFalse();
    }

    @Test
    @DisplayName("Enchantments an opponent controls are not animated")
    void doesNotAnimateOpponentEnchantments() {
        addFiveEnchantments();
        harness.addToBattlefield(player2, new SylvanEchoes());

        Permanent opponentEchoes = findPermanent(player2, "Sylvan Echoes");

        assertThat(gqs.isCreature(gd, opponentEchoes)).isFalse();
    }
}
