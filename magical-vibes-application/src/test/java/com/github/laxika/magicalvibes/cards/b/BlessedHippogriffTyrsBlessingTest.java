package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedHippogriffTyrsBlessing.class, GrizzlyBears.class})
class BlessedHippogriffTyrsBlessingTest extends BaseCardTest {

    @Test
    void attackingCreatureWithoutFlyingGainsFlyingUntilEndOfTurn() {
        addReady(player1, new BlessedHippogriffTyrsBlessing());
        Permanent attacker = addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void attackTriggerCannotTargetCreatureWithFlying() {
        Permanent hippogriff = addReady(player1, new BlessedHippogriffTyrsBlessing());
        addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hippogriff.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureGrantsIndestructibleUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BlessedHippogriffTyrsBlessing card = new BlessedHippogriffTyrsBlessing();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
