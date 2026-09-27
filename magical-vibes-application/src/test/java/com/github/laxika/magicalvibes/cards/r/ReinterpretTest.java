package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reinterpret.class, AirElemental.class, Divination.class, HillGiant.class})
class ReinterpretTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndOffersEqualOrLowerManaValueSpell() {
        HillGiant target = new HillGiant();
        HillGiant eligible = new HillGiant();
        AirElemental tooExpensive = new AirElemental();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible, tooExpensive));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(tooExpensive);
    }

    @Test
    void decliningFreeCastLeavesEligibleSpellInHand() {
        HillGiant target = new HillGiant();
        Divination eligible = new Divination();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), eligible));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).contains(eligible);
    }

    @Test
    void doesNotOfferSpellAboveTargetManaValue() {
        HillGiant target = new HillGiant();
        AirElemental tooExpensive = new AirElemental();

        harness.castFromHand(player1, target, "{3}{R}");
        harness.setHand(player2, List.of(new Reinterpret(), tooExpensive));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(tooExpensive);
    }

    @Test
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Reinterpret()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Hill Giant")))
                .isInstanceOf(IllegalStateException.class);
    }
}
