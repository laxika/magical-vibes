package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaFlow.class, Forest.class, GrizzlyBears.class, AuraOfSilence.class, Unsummon.class})
class LavaFlowTest extends BaseCardTest {

    @Test
    @DisplayName("Lava Flow destroys target creature")
    void destroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lava Flow destroys target land")
    void destroysTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0, forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Lava Flow cannot target a noncreature, nonland permanent")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is playable
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land");
    }

    @Test
    @DisplayName("Lava Flow allows a regeneration shield to save the target creature")
    void allowsRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(bears.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Lava Flow can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lava Flow");
    }

    @Test
    @DisplayName("Lava Flow does nothing when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lava Flow");
        assertThat(gd.stack).isEmpty();
    }
}
