package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesertionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and puts it onto the battlefield under Desertion's controller")
    void countersCreatureAndGainsControl() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The countered creature enters under player2's control, not into player1's graveyard/library.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // player1 is still the owner (recorded so the card returns to them when it leaves play).
        assertThat(gd.stolenCreatures).containsValue(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a non-artifact/creature spell into its owner's graveyard")
    void countersNoncreatureIntoGraveyard() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        int startingLife = harness.getGameData().getLife(player1.getId());

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mercy.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angel's Mercy"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angel's Mercy"));
        // Countered, so its life-gain effect never resolved.
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Desertion"));
    }
}
