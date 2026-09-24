package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Archnemesis.class, GrizzlyBears.class})
class ArchnemesisTest extends BaseCardTest {

    @Test
    void attackingEnchantedPlayerCausesLifeLossDrawAndLifeGain() {
        Permanent aura = attachToPlayer2();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveTopTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(aura.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void mayAttachToPlayerWhoAttacksController() {
        Permanent aura = attachToPlayer2();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveTopTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getAttachedTo()).isEqualTo(player2.getId());
    }

    private Permanent attachToPlayer2() {
        Permanent aura = new Permanent(new Archnemesis());
        aura.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void resolveTopTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
