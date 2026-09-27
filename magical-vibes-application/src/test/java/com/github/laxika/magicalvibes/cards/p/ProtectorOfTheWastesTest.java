package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

@CardUsed({ProtectorOfTheWastes.class, Bonesplitter.class, GloriousAnthem.class})
class ProtectorOfTheWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Protector of the Wastes exiles up to two artifacts or enchantments controlled by different players on ETB")
    void entersAndExilesTargetsControlledByDifferentPlayers() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();
        harness.castCreature(player1, 0, List.of(ownArtifact.getId(), opposingEnchantment.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Protector of the Wastes cannot target two permanents controlled by the same player")
    void cannotTargetTwoPermanentsControlledBySamePlayer() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Protector of the Wastes exiles targets when it becomes monstrous")
    void becomingMonstrousExilesTargets() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new ProtectorOfTheWastes());
        protector.setSummoningSick(false);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.handlePermanentChosen(player1, opposingEnchantment.getId());
        harness.passBothPriorities();

        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(protector.isMonstrous()).isTrue();
        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
