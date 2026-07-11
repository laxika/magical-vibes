package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DeathPitsOfRath;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringCleaningTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpringCleaning()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // {1}{G}
    }

    // Caster wins: their revealed top card (Grizzly Bears, MV 2) beats the opponent's (Forest, MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash destroys the target and all other enchantments opponents control")
    void wonClashDestroysAllOpponentEnchantments() {
        prepare();
        harness.addToBattlefield(player2, new AngelicChorus());   // target enchantment
        harness.addToBattlefield(player2, new DeathPitsOfRath()); // other opponent enchantment
        harness.addToBattlefield(player1, new AngelicChorus());   // own enchantment, must survive
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"))
                .noneMatch(p -> p.getCard().getName().equals("Death Pits of Rath"));
        // The caster's own enchantment is untouched.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Losing the clash destroys only the target enchantment")
    void lostClashDestroysOnlyTarget() {
        prepare();
        harness.addToBattlefield(player2, new AngelicChorus());   // target enchantment
        harness.addToBattlefield(player2, new DeathPitsOfRath()); // survives on a loss
        stackClashLossForCaster();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"))
                .anyMatch(p -> p.getCard().getName().equals("Death Pits of Rath"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        prepare();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
