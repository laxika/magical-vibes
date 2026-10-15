package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NyxbornBrute;
import com.github.laxika.magicalvibes.cards.p.PhoenixOfAsh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderworldBreach.class, Shock.class, Mountain.class, NyxbornBrute.class, PhoenixOfAsh.class})
class UnderworldBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a nonland card from the graveyard by exiling three other cards")
    void castsSpellWithEscape() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Cannot cast a spell with escape without three other graveyard cards")
    void requiresThreeOtherGraveyardCards() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("escape");
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of an opponent's end step")
    void sacrificesAtNextEndStep() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Underworld Breach");
        harness.assertInGraveyard(player1, "Underworld Breach");
    }

    @Test
    @DisplayName("An escaped instant can be cast again by paying escape again")
    void canEscapeSameInstantAgain() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Shock(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Shock");

        int shockIndex = gd.playerGraveyards.get(player1.getId()).size() - 1;
        gs.playFlashbackSpell(gd, player1, shockIndex, null, player2.getId(), List.of(), List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("A nonland permanent escapes for its mana cost and enters the battlefield")
    void escapesPermanent() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new NyxbornBrute(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Brute");
        harness.assertNotInGraveyard(player1, "Nyxborn Brute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Breach's escape cost can be chosen instead of a card's printed escape cost")
    void choosesGrantedEscapeOverPrintedEscape() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new PhoenixOfAsh(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phoenix of Ash");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Escape does not let permanent spells be cast during an opponent's turn")
    void respectsPermanentTiming() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new NyxbornBrute(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Nyxborn Brute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Breach does not grant escape to lands")
    void cannotEscapeLand() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Breach does not grant escape to your graveyard")
    void permissionOnlyForController() {
        harness.addToBattlefield(player2, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new NyxbornBrute(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Nyxborn Brute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Breach sacrifices itself during its controller's end step too")
    void sacrificesDuringControllerEndStep() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Underworld Breach");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Underworld Breach");
        harness.assertInGraveyard(player1, "Underworld Breach");
    }

    @Test
    @DisplayName("Escape still requires the full mana cost of the nonland card")
    void requiresFullManaCost() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new NyxbornBrute(), new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Nyxborn Brute");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
