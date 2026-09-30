package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenImpact.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        AirElemental.class})
class MoltenImpactTest extends BaseCardTest {

    @Test
    void excessDamageCreatesOneShotSpellBoonWithTheNotedAmount() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(boonTarget.getId()).doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();

        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exactDamageDoesNotCreateTheBoon() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent unaffectedTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(unaffectedTarget.getMarkedDamage()).isZero();
    }
}
