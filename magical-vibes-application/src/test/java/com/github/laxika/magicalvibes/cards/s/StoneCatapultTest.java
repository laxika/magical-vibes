package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target tapped nonblack creature")
    void resolvingDestroysTappedNonblackCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTapped(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a tapped black creature")
    void cannotTargetBlackCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTapped(player2, blackCreature());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an untapped nonblack creature")
    void cannotTargetUntappedCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addUntapped(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupCatapultOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addTapped(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = addTapped(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupCatapultOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new StoneCatapult());
        findPermanent(player1, "Stone Catapult").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.tap();
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addUntapped(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card blackCreature() {
        Card card = new Card();
        card.setName("Bog Imp");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
