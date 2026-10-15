package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingbeatWarrior.class, FugitiveWizard.class})
class WingbeatWarriorTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesTargetCreatureFirstStrikeUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent warrior = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warrior));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void turningFaceUpCanTargetTheWarriorItself() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent warrior = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warrior));
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castingFaceUpDoesNotGrantFirstStrike() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new WingbeatWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent warrior = findPermanent(player1, "Wingbeat Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void firstStrikeIsGrantedOnlyWhenTheFaceUpTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent warrior = castFaceDown();
        assertThat(warrior.isFaceDown()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warrior));
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(warrior.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new WingbeatWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Wingbeat Warrior");
    }
}
