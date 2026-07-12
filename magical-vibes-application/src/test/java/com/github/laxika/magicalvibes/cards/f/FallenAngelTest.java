package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FallenAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature to the ability gives Fallen Angel +2/+1")
    void resolvingAbilityBoostsAngel() {
        addFallenAngelReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Grizzly Bears is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        Permanent angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angel.getCard().getName()).isEqualTo("Fallen Angel");
        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability stacks when activated multiple times")
    void canActivateMultipleTimes() {
        addFallenAngelReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID first = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first);
        harness.passBothPriorities();

        UUID second = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second);
        harness.passBothPriorities();

        Permanent angel = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angel.getCard().getName()).isEqualTo("Fallen Angel");
        assertThat(angel.getPowerModifier()).isEqualTo(4);
        assertThat(angel.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fallen Angel can sacrifice itself to its own ability")
    void canSacrificeItself() {
        addFallenAngelReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fallen Angel"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fallen Angel"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addFallenAngelReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent angel = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getPowerModifier()).isEqualTo(0);
        assertThat(angel.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addFallenAngelReady(Player player) {
        FallenAngel card = new FallenAngel();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
