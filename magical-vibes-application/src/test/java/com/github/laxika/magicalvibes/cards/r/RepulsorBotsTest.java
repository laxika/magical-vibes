package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RepulsorBots.class, GrizzlyBears.class, Spellbook.class, Island.class})
class RepulsorBotsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to two other artifacts and/or creatures")
    void etbReturnsTwoMixedTargets() {
        UUID creatureId = addCreatureReady(player1, new GrizzlyBears()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new RepulsorBots()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        UUID sourceId = harness.getPermanentId(player1, "Repulsor Bots");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creatureId, artifactId)
                .doesNotContain(sourceId);

        harness.handlePermanentChosen(player1, creatureId);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("The ETB can return only one target")
    void etbCanReturnOneTarget() {
        UUID creatureId = addCreatureReady(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new RepulsorBots()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB cannot target itself or a land")
    void etbExcludesSourceAndLands() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new RepulsorBots()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertOnBattlefield(player1, "Island");
    }
}
