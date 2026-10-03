package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        addCreatureReady(player1, new BlessedHippogriffTyrsBlessing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent hippogriff = addCreatureReady(player1, new BlessedHippogriffTyrsBlessing());
        addCreatureReady(player1, new GrizzlyBears());

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

    @Test
    void attackTriggerCannotTargetNonattackingCreature() {
        addCreatureReady(player1, new BlessedHippogriffTyrsBlessing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonattacker.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void adventureCanTargetOpponentsCreatureAndCreatureCanLaterBeCastFromExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlessedHippogriffTyrsBlessing());
        BlessedHippogriffTyrsBlessing card = new BlessedHippogriffTyrsBlessing();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blessed Hippogriff").getCard()).isSameAs(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureWithDepartedTargetGoesToGraveyardInsteadOfExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlessedHippogriffTyrsBlessing());
        BlessedHippogriffTyrsBlessing card = new BlessedHippogriffTyrsBlessing();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
