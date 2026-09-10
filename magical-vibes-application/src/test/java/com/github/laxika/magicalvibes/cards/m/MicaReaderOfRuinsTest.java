package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MicaReaderOfRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant prompts the optional artifact sacrifice")
    void castingInstantPromptsArtifactSacrifice() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting and sacrificing an artifact creates a copy")
    void sacrificingArtifactCreatesCopy() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var artifact = findPermanent(player1, "Iron Myr");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Iron Myr"));
        long boltCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining does not sacrifice an artifact or create a copy")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Iron Myr"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells do not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
