package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OracleOfBonesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts two +1/+1 counters on Oracle of Bones and does not offer a spell")
    void tributePaid() {
        castOracle(List.of());

        harness.handleMayAbilityChosen(player2, true);

        Permanent oracle = findPermanent(player1, "Oracle of Bones");
        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining tribute offers an instant or sorcery from hand for free")
    void tributeNotPaidOffersInstantOrSorcery() {
        Divination spell = new Divination();
        castOracle(List.of(spell, new GrizzlyBears()));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(findPermanent(player1, "Oracle of Bones")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the free cast leaves the instant or sorcery in hand")
    void decliningFreeCastLeavesSpellInHand() {
        Divination spell = new Divination();
        castOracle(List.of(spell));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void castOracle(List<com.github.laxika.magicalvibes.model.Card> hand) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>(hand);
        cards.add(0, new OracleOfBones());
        harness.setHand(player1, cards);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
