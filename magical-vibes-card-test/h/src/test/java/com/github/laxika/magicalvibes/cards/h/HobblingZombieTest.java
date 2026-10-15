package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ComponentCollector;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HobblingZombie.class, ComponentCollector.class})
class HobblingZombieTest extends BaseCardTest {

    @Test
    @DisplayName("When Hobbling Zombie dies, it creates a 2/2 black Zombie token with decayed")
    void createsDecayedZombieWhenItDies() {
        Permanent hobblingZombie = harness.addToBattlefieldAndReturn(player1, new HobblingZombie());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hobblingZombie));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(bls.canBlock(gd, token)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Hobbling Zombie creates its token for that opponent")
    void deathCreatesTokenForController() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new HobblingZombie());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, zombie));

        assertThat(countPermanents(player2, "Zombie")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertInGraveyard(player2, "Hobbling Zombie");
        assertThat(findPermanent(player2, "Zombie").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exiling Hobbling Zombie does not trigger its death ability")
    void exileDoesNotCreateToken() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new HobblingZombie());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, zombie));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertNotInGraveyard(player1, "Hobbling Zombie");
    }

    @Test
    @DisplayName("Only the attacking decayed token is sacrificed when its end of combat trigger resolves")
    void decayedSacrificesOnlyAttackingTokenAtEndOfCombat() {
        for (int i = 0; i < 2; i++) {
            Permanent zombie = harness.addToBattlefieldAndReturn(player1, new HobblingZombie());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, zombie));
            resolveAllTriggers();
        }
        List<Permanent> tokens = findPermanents(player1, "Zombie");
        Permanent attacker = tokens.getFirst();
        attacker.setSummoningSick(false);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(attackerIndex));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(tokens);
        });

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(tokens);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(attacker).contains(tokens.get(1));
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Hobbling Zombie's deathtouch kills a blocker with more toughness than its power")
    void deathtouchKillsLargerBlocker() {
        addCreatureReady(player1, new HobblingZombie());
        harness.addToBattlefield(player2, new ComponentCollector());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Component Collector");
        harness.assertNotOnBattlefield(player2, "Component Collector");
        harness.assertOnBattlefield(player1, "Hobbling Zombie");
        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertLife(player2, 20);
    }
}
