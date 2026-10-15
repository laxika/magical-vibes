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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnTrail.class, BoggartArsonists.class})
class BurnTrailTest extends BaseCardTest {

    @Test
    @DisplayName("Conspire can tap summoning-sick creatures and keep the original target")
    void conspireWithSummoningSickCreaturesKeepsTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoggartArsonists());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoggartArsonists());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.castWithConspire(player1, 0, player2.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Conspire cannot tap the same creature twice")
    void conspireRequiresDistinctCreatures() {
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent creature = addCreatureReady(player1, new BoggartArsonists());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, player2.getId(),
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Conspire cannot use an already tapped creature")
    void conspireRequiresUntappedCreatures() {
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent first = addCreatureReady(player1, new BoggartArsonists());
        Permanent second = addCreatureReady(player1, new BoggartArsonists());
        second.tap();

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

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
