package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.r.RangersPath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YevaNaturesHerald.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class,
        Fog.class, RangersPath.class, PaintersServant.class})
class YevaNaturesHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Yeva can be cast during combat without another Yeva on the battlefield")
    void yevaHasHerOwnFlash() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new YevaNaturesHerald()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yeva, Nature's Herald");
    }

    @Test
    @DisplayName("A green creature can be cast in response to a spell")
    void castsGreenCreatureWithNonemptyStack() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());
        harness.setHand(player1, List.of(new Fog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Yeva does not allow green sorceries to be cast during combat")
    void cannotCastGreenNoncreatureSpell() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RangersPath()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Painter's Servant makes an originally red creature eligible for Yeva's permission")
    void usesCurrentColorsOfCreatureSpells() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());
        var servant = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        servant.setChosenColor(CardColor.GREEN);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Can cast a green creature spell during the opponent's turn")
    void canCastGreenCreatureDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast a green creature spell during combat")
    void canCastGreenCreatureDuringCombat() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast a non-green creature spell at instant speed")
    void cannotCastNonGreenCreature() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Green creature spells lose flash timing when Yeva leaves the battlefield")
    void losesFlashWhenYevaLeaves() {
        harness.addToBattlefield(player1, new YevaNaturesHerald());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Only Yeva's controller may cast green creature spells at instant speed")
    void onlyAffectsController() {
        harness.addToBattlefield(player2, new YevaNaturesHerald());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
