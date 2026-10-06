package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengedBlade.class, CliffhavenSellSword.class})
class ScavengedBladeTest extends BaseCardTest {

    @Test
    void entersAttachedToTargetCreatureAndBoostsIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new ScavengedBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Scavenged Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentCreatureOnEntry() {
        harness.addToBattlefield(player1, new CliffhavenSellSword());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new ScavengedBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipAttachesBladeToAnotherCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ScavengedBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void canEnterWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new ScavengedBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Scavenged Blade").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryTriggerDoesNotAttachIfTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new ScavengedBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Scavenged Blade").getAttachedTo()).isNull();
    }

    @Test
    void equippingAnotherCreatureMovesThePowerBonus() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ScavengedBlade());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        blade.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }
}
