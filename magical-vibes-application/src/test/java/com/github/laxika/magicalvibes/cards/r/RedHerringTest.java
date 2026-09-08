package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedHerring.class, GrizzlyBears.class})
class RedHerringTest extends BaseCardTest {

    @Test
    @DisplayName("Red Herring must attack each combat when able")
    void mustAttackWhenAble() {
        addReadyRedHerring();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Paying two mana sacrifices Red Herring and draws a card")
    void sacrificesAndDraws() {
        Permanent redHerring = addReadyRedHerring();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(redHerring);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(redHerring.getCard());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyRedHerring() {
        return addCreatureReady(player1, new RedHerring());
    }
}
