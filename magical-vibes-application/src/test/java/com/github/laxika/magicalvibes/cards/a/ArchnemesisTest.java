package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void canBeCastEnchantingOpponent() {
        prepareAuraCast();

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Archnemesis").getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void cannotBeCastEnchantingItsController() {
        prepareAuraCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleAttackersTriggerLifeLossDrawAndLifeGainOnlyOnce() {
        attachToPlayer2();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        resolveTopTrigger();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackTriggerStillResolvesAfterAttackerLeavesBattlefield() {
        attachToPlayer2();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        resolveTopTrigger();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void attackTriggerStillResolvesAfterAuraLeavesBattlefield() {
        Permanent aura = attachToPlayer2();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveTopTrigger();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineAttachmentWhenAttackedByMultipleCreatures() {
        Permanent aura = attachToPlayer2();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        resolveTopTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS, () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(aura.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void prepareAuraCast() {
        harness.setHand(player1, List.of(new Archnemesis()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent attachToPlayer2() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Archnemesis());
        aura.setAttachedTo(player2.getId());
        return aura;
    }

    private void resolveTopTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
