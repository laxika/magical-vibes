package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ExquisiteBlood;
import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaerveksSpite.class, ExquisiteBlood.class, GossamerChains.class, Impulse.class,
        PhyrexianWalker.class})
class KaerveksSpiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices all your permanents, discards your hand, and target loses 5 life")
    void castingPaysCostsAndTargetLosesFiveLife() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksSpite(), new Impulse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.addToBattlefield(player1, new GossamerChains());
        harness.addToBattlefield(player2, new PhyrexianWalker());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Phyrexian Walker");
        harness.assertInGraveyard(player1, "Gossamer Chains");
        harness.assertInGraveyard(player1, "Impulse");
        harness.assertInGraveyard(player1, "Kaervek's Spite");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Phyrexian Walker");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Additional costs are paid before the spell resolves")
    void additionalCostsArePaidBeforeResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksSpite(), new Impulse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.addToBattlefield(player1, new GossamerChains());

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Phyrexian Walker");
        harness.assertInGraveyard(player1, "Gossamer Chains");
        harness.assertNotInGraveyard(player1, "Kaervek's Spite");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kaervek's Spite");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can cast with no permanents and empty remaining hand")
    void canCastWithEmptyBoardAndHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Kaervek's Spite");
    }

    @Test
    @DisplayName("Targeted life loss is recorded as life lost this turn")
    void targetedLifeLossIsRecordedForTheTurn() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.lifeLostThisTurn.get(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Targeted life loss triggers an opponent's life-loss ability")
    void targetedLifeLossTriggersOpponentLifeLossAbility() {
        harness.addToBattlefield(player2, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KaerveksSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new KaerveksSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Phyrexian Walker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("If mana payment fails, costs are not paid")
    void manaPaymentFailureDoesNotPayCosts() {
        harness.setHand(player1, List.of(new KaerveksSpite(), new Impulse()));
        harness.addToBattlefield(player1, new PhyrexianWalker());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phyrexian Walker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Phyrexian Walker");
        harness.assertNotInGraveyard(player1, "Impulse");
    }
}
