package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfTheWaningMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell gives target creature fear")
    void spiritCastGrantsFear() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new KamiOfTheWaningMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent(bearsId), Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell gives target creature fear")
    void arcaneCastGrantsFear() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent(bearsId), Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOff() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent(bearsId), Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent permanent(UUID id) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }
}
