package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReliquaryMonkTest extends BaseCardTest {

    @Test
    @DisplayName("When Reliquary Monk dies, it destroys target artifact")
    void diesDestroysTargetArtifact() {
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new LeoninScimitar());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");

        harness.castInstant(player2, 0, monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reliquary Monk");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("When Reliquary Monk dies, it destroys target enchantment")
    void diesDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new GloriousAnthem());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.castInstant(player2, 0, monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enchantmentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reliquary Monk");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Death trigger only offers artifacts and enchantments as valid targets")
    void targetFilterOnlyArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new ReliquaryMonk());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID monkId = harness.getPermanentId(player1, "Reliquary Monk");
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.castInstant(player2, 0, monkId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(artifactId, enchantmentId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
