package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconAbomination.class})
class FalconAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("When Falcon Abomination enters, it creates a decayed Zombie token")
    void etbCreatesDecayedZombieToken() {
        harness.setHand(player1, List.of(new FalconAbomination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast creates the token for the entering creature's controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new FalconAbomination());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(findPermanent(player2, "Zombie").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the attacking Zombie is sacrificed when its end of combat trigger resolves")
    void decayedSacrificesOnlyAttackingTokenAfterCombat() {
        harness.enterBattlefieldAndReturn(player1, new FalconAbomination());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new FalconAbomination());
        resolveAllTriggers();
        List<Permanent> zombies = findPermanents(player1, "Zombie");
        Permanent attacker = zombies.getFirst();
        attacker.setSummoningSick(false);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(attackerIndex));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(zombies);
        });

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(zombies);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(attacker).contains(zombies.get(1));
    }
}
