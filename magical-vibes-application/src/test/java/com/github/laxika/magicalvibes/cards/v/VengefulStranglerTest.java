package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.cards.s.StranglingGrasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulStrangler.class, GrizzlyBears.class, SearingSpear.class})
class VengefulStranglerTest extends BaseCardTest {

    @Test
    void diesAndReturnsTransformedAttachedToTargetOpponentCreature() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new VengefulStrangler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, strangler.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(strangler.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(StranglingGrasp.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void enchantedControllerSacrificesNonlandPermanentThenLosesLifeAtUpkeep() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent sacrifice = addCreatureReady(player2, new GrizzlyBears());
        Permanent grasp = new Permanent(new VengefulStrangler());
        grasp.setCard(grasp.getOriginalCard().getBackFaceCard());
        grasp.setTransformed(true);
        grasp.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(grasp);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrifice.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted).doesNotContain(sacrifice);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
