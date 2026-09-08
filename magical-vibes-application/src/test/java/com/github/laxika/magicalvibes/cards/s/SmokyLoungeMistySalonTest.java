package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.CharredFoyerWarpedSpace;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokyLoungeMistySalon.class, BloodMoon.class, CharredFoyerWarpedSpace.class})
class SmokyLoungeMistySalonTest extends BaseCardTest {

    @Test
    void smokyLoungeAddsTwoRedRoomOnlyManaAtTheControllersFirstMainPhase() {
        castSmokyLounge();

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId())
                .getRoomSpellsOrUnlocksMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void roomOnlyManaCanCastARoomButNotAnotherEnchantment() {
        castSmokyLounge();
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new BloodMoon()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new CharredFoyerWarpedSpace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM));
    }

    @Test
    void mistySalonCreatesAnXSpiritUsingAllUnlockedRoomDoors() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon(), new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(spirit.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.FLYING);
    }

    private void castSmokyLounge() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
