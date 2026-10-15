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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

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

    @Test
    @DisplayName("Declining the copy leaves no generated card in any zone")
    void decliningCopyLeavesNoGeneratedCard() {
        harness.castFromHand(player1, new JundEmOut(), "{B}{R}{G}");
        harness.passBothPriorities();
        var copyId = gd.pendingMayAbilities.getFirst().sourceCard().getId();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId()).doesNotContain(copyId);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId()).doesNotContain(copyId);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Jund 'Em Out");
    }

    @Test
    @DisplayName("Retrace cannot discard a nonland card")
    void retraceRejectsNonlandDiscard() {
        JundEmOut card = new JundEmOut();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new JundEmOut()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Jund 'Em Out");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The same card can be retraced again after resolving")
    void retraceCanBeRepeated() {
        JundEmOut card = new JundEmOut();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);

        for (int cast = 0; cast < 2; cast++) {
            var graveyard = gd.playerGraveyards.get(player1.getId());
            int graveyardIndex = java.util.stream.IntStream.range(0, graveyard.size())
                    .filter(index -> graveyard.get(index).getId().equals(card.getId())).findFirst().orElseThrow();
            harness.ensurePriority(player1);
            harness.castRetrace(player1, graveyardIndex, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(graveyardCard -> graveyardCard.getId())
                .contains(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(graveyardCard -> graveyardCard instanceof Forest).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
