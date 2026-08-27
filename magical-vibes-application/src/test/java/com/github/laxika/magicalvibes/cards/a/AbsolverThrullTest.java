package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsolverThrull.class, GloriousAnthem.class, GrizzlyBears.class, LightningBolt.class})
class AbsolverThrullTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");
        castAbsolver(enchantmentId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void deathExilesItHauntingTargetCreature() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        castAbsolver(enchantmentId);

        UUID absolverId = harness.getPermanentId(player1, "Absolver Thrull");
        destroyWithLightningBolt(absolverId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Absolver Thrull");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Absolver Thrull");
    }

    @Test
    void hauntedCreatureDeathDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID firstEnchantmentId = findPermanents(player2, "Glorious Anthem").getFirst().getId();
        UUID secondEnchantmentId = findPermanents(player2, "Glorious Anthem").get(1).getId();
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        castAbsolver(firstEnchantmentId);

        UUID absolverId = harness.getPermanentId(player1, "Absolver Thrull");
        destroyWithLightningBolt(absolverId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        destroyWithLightningBolt(creatureId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondEnchantmentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    private void castAbsolver(UUID targetId) {
        setupPlayer1Active();
        harness.setHand(player1, List.of(new AbsolverThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithLightningBolt(UUID targetId) {
        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void setupPlayer1Active() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
