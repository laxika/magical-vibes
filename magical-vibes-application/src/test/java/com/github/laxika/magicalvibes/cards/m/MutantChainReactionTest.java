package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MutantChainReaction.class, GreaterAuramancy.class, GrizzlyBears.class,
        MindStone.class, ShivanDragon.class})
class MutantChainReactionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and creates a Mutagen token")
    void destroysArtifactAndCreatesMutagen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castWithTarget(target);

        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertOnBattlefield(player1, "Mutagen");
    }

    @Test
    @DisplayName("Destroys an enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());

        castWithTarget(target);

        harness.assertInGraveyard(player2, "Greater Auramancy");
    }

    @Test
    @DisplayName("Destroys a creature with flying")
    void destroysFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());

        castWithTarget(target);

        harness.assertInGraveyard(player2, "Shivan Dragon");
    }

    @Test
    @DisplayName("Can be cast without choosing a target")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new MutantChainReaction()));
        addManaForSpell();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mutagen");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MutantChainReaction()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    @Test
    @DisplayName("A Mutagen can be sacrificed to put a +1/+1 counter on a creature")
    void mutagenCanPutCounterOnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithoutTarget();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Mutagen");
    }

    private void castWithTarget(Permanent target) {
        harness.setHand(player1, List.of(new MutantChainReaction()));
        addManaForSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castWithoutTarget() {
        harness.setHand(player1, List.of(new MutantChainReaction()));
        addManaForSpell();
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
