package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AbruptDecay;
import com.github.laxika.magicalvibes.cards.b.Blightning;
import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        JundEmOut.class,
        AbruptDecay.class,
        Blightning.class,
        BloodbraidElf.class,
        LightningBolt.class,
        LilianaOfTheVeil.class,
        Tarmogoyf.class,
        GrizzlyBears.class,
        Forest.class
})
class JundEmOutTest extends BaseCardTest {

    private static final Set<String> JUND_CARD_NAMES = Set.of(
            "Abrupt Decay", "Blightning", "Bloodbraid Elf", "Lightning Bolt",
            "Liliana of the Veil", "Tarmogoyf");

    @Test
    @DisplayName("Creates a random listed copy and offers it for a free cast")
    void createsRandomCopyAndOffersFreeCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new JundEmOut(), "{B}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        var ability = gd.pendingMayAbilities.getFirst();
        assertThat(JUND_CARD_NAMES).contains(ability.sourceCard().getName());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .contains(ability.sourceCard().getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .doesNotContain(ability.sourceCard().getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Retrace discards a land and resolves the random-copy choice")
    void retraceDiscardsLand() {
        JundEmOut card = new JundEmOut();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Jund 'Em Out");
        harness.assertInGraveyard(player1, "Forest");
    }
}
