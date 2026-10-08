package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarrenWarleader.class, GrizzlyBears.class, RoostOfDrakes.class})
class WarrenWarleaderTest extends BaseCardTest {

    private static final String TOKEN_MODE =
            "Create a 1/1 white Rabbit creature token that's tapped and attacking.";
    private static final String BOOST_MODE =
            "Attacking creatures you control get +1/+1 until end of turn.";

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new WarrenWarleader()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void attackTriggerCreatesTappedAttackingRabbit() {
        addCreatureReady(player1, new WarrenWarleader());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, TOKEN_MODE);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
    }

    @Test
    void attackTriggerBoostsOnlyYourAttackersUntilEndOfTurn() {
        addCreatureReady(player1, new WarrenWarleader());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleListChoice(player1, BOOST_MODE);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void noOffspringCopyWithoutPayingAdditionalCost() {
        harness.setHand(player1, List.of(new WarrenWarleader()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void triggersWhenOtherCreaturesAttackWhileWarleaderStaysBack() {
        Permanent warleader = addCreatureReady(player1, new WarrenWarleader());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();
        harness.handleListChoice(player1, BOOST_MODE);

        assertThat(firstAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(secondAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(warleader.getEffectivePower()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void offspringCopyAlsoTriggersWhenOnlyAnotherCreatureAttacks() {
        harness.setHand(player1, List.of(new WarrenWarleader()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2));
        resolveAllTriggers();
        harness.handleListChoice(player1, BOOST_MODE);
        resolveAllTriggers();
        harness.handleListChoice(player1, BOOST_MODE);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new WarrenWarleader()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Drake")).isZero();
        assertThat(findPermanents(player1, "Warren Warleader")).hasSize(2);
    }
}
