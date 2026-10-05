package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RetractionHelix;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OracleOfBones.class, Divination.class, SwordwiseCentaur.class, RetractionHelix.class, Silence.class})
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
        castOracle(List.of(spell, new SwordwiseCentaur()));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof SwordwiseCentaur);
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

    @Test
    @CardUsed({Silence.class})
    void silencePreventsFreeCastWhileTriggerStillResolves() {
        Divination spell = new Divination();
        castOracle(List.of(spell));
        harness.handleMayAbilityChosen(player2, false);

        harness.castFromHand(player2, new Silence(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        harness.assertOnBattlefield(player1, "Oracle of Bones");
    }

    @Test
    void acceptingOneSpellDoesNotOfferAnother() {
        Divination first = new Divination();
        Divination second = new Divination();
        castOracle(List.of(first, second));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void decliningFirstSpellStillAllowsSecond() {
        Divination first = new Divination();
        Divination second = new Divination();
        castOracle(List.of(first, second));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    void creatureOnlyHandDoesNotOfferFreeCast() {
        SwordwiseCentaur creature = new SwordwiseCentaur();
        castOracle(List.of(creature));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void canCastTargetedInstantWithoutBlueMana() {
        RetractionHelix spell = new RetractionHelix();
        castOracle(List.of(spell));

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent oracle = findPermanent(player1, "Oracle of Bones");
        harness.handlePermanentChosen(player1, oracle.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Retraction Helix");
    }

    private void castOracle(List<com.github.laxika.magicalvibes.model.Card> hand) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new OracleOfBones(), "{2}{R}{R}");
        harness.setHand(player1, hand);
        harness.passBothPriorities();
    }
}
