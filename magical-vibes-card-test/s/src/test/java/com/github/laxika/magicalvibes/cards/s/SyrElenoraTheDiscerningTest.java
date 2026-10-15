package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KenrithTheReturnedKing;
import com.github.laxika.magicalvibes.cards.t.TomeRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrElenoraTheDiscerning.class, Forest.class, TomeRaider.class,
        ScorchingDragonfire.class, KenrithTheReturnedKing.class})
class SyrElenoraTheDiscerningTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in hand while toughness stays 4")
    void powerEqualsHandSizeAndToughnessStaysFour() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new TomeRaider(), new TomeRaider()));

        assertThat(gqs.getEffectivePower(gd, elenora)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elenora)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters-the-battlefield ability draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new SyrElenoraTheDiscerning()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Opponent's spell targeting Syr Elenora costs 2 more")
    void opponentSpellTargetingElenoraCostsMore() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ScorchingDragonfire()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elenora.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Syr Elenora does not tax its controller's spell")
    void ownSpellTargetingElenoraIsNotTaxed() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.setHand(player1, List.of(new ScorchingDragonfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, elenora.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Power updates as the controller's hand becomes empty or gains cards")
    void powerTracksHandChanges() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, elenora)).isZero();

        harness.setHand(player1, List.of(new Forest(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, elenora)).isEqualTo(2);

        harness.setHand(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, elenora)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power is defined in hand and counts Syr Elenora itself")
    void powerIsDefinedInHand() {
        SyrElenoraTheDiscerning elenora = new SyrElenoraTheDiscerning();
        harness.setHand(player1, List.of(elenora, new Forest()));
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, elenora)).isEqualTo(2);

        harness.setHand(player1, List.of(elenora));
        assertThat(gqs.getEffectiveCardPower(gd, elenora)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent can pay the targeting tax and resolve the spell")
    void opponentCanPayTargetingTax() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ScorchingDragonfire()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, elenora.getId());

        assertThat(elenora.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent's spell targeting another creature is not taxed")
    void otherCreatureIsNotTaxed() {
        harness.addToBattlefield(player1, new SyrElenoraTheDiscerning());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TomeRaider());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ScorchingDragonfire()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, other.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent's activated ability is not taxed and counters modify the characteristic power")
    void opponentActivatedAbilityIsNotTaxed() {
        Permanent elenora = harness.addToBattlefieldAndReturn(player1, new SyrElenoraTheDiscerning());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new KenrithTheReturnedKing());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, 1, null, elenora.getId());
        harness.passBothPriorities();

        assertThat(elenora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elenora)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elenora)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
