package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurnTrail.class, BoggartArsonists.class})
class BurnTrailTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target player")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature, destroying it")
    void dealsThreeDamageToCreature() {
        Permanent target = addCreatureReady(player2, new BoggartArsonists());
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Boggart Arsonists");
        harness.assertInGraveyard(player2, "Boggart Arsonists");
    }

    @Test
    @DisplayName("Conspire taps two red creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent arsonist1 = addCreatureReady(player1, new BoggartArsonists());
        Permanent arsonist2 = addCreatureReady(player1, new BoggartArsonists());

        harness.castWithConspire(player1, 0, player2.getId(),
                List.of(arsonist1.getId(), arsonist2.getId()));

        assertThat(arsonist1.isTapped()).isTrue();
        assertThat(arsonist2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire copy may retarget and both spell instances resolve")
    void conspireCopyCanChooseNewTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent target = addCreatureReady(player2, new BoggartArsonists());
        Permanent arsonist1 = addCreatureReady(player1, new BoggartArsonists());
        Permanent arsonist2 = addCreatureReady(player1, new BoggartArsonists());

        harness.castWithConspire(player1, 0, player2.getId(),
                List.of(arsonist1.getId(), arsonist2.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Boggart Arsonists");
        harness.assertInGraveyard(player2, "Boggart Arsonists");
    }
}
