package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanCaryatid.class, VoyagesEnd.class})
class SylvanCaryatidTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Sylvan Caryatid while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SylvanCaryatid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Sylvan Caryatid prompts for mana color immediately")
    void activateAbilityPromptsManaColorImmediately() {
        Permanent caryatid = addCreatureReady(player1, new SylvanCaryatid());

        harness.activateAbility(player1, 0, null, null);

        assertThat(caryatid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent caryatid = harness.addToBattlefieldAndReturn(player1, new SylvanCaryatid());
            GameData gd = harness.getGameData();
            caryatid.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Sylvan Caryatid when it is already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new SylvanCaryatid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Defender prevents Sylvan Caryatid from attacking")
    void cannotAttackWithDefender() {
        Permanent caryatid = addCreatureReady(player1, new SylvanCaryatid());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caryatid.isAttacking()).isFalse();
        assertThat(caryatid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Sylvan Caryatid")
    void opponentCannotTargetCaryatid() {
        Permanent caryatid = addCreatureReady(player1, new SylvanCaryatid());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, caryatid.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.assertOnBattlefield(player1, "Sylvan Caryatid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof allows its controller to target Sylvan Caryatid")
    void controllerCanTargetCaryatid() {
        Permanent caryatid = addCreatureReady(player1, new SylvanCaryatid());
        harness.setHand(player1, List.of(new VoyagesEnd()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, caryatid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sylvan Caryatid");
        harness.assertInHand(player1, "Sylvan Caryatid");
    }
}
