package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanguardOfTheRose.class, GrizzlyBears.class, Spellbook.class})
class VanguardOfTheRoseTest extends BaseCardTest {

    @Test
    void sacrificingAnotherCreatureGrantsIndestructibleAndTaps() {
        Permanent vanguard = addReady(new VanguardOfTheRose());
        Permanent sacrificed = addReady(new GrizzlyBears());
        addMana();

        harness.activateAbility(player1, battlefieldIndex(vanguard), null, null);
        harness.passBothPriorities();

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    void sacrificingAnotherArtifactGrantsIndestructibleAndTaps() {
        Permanent vanguard = addReady(new VanguardOfTheRose());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        addMana();

        harness.activateAbility(player1, battlefieldIndex(vanguard), null, null);
        harness.passBothPriorities();

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    void cannotActivateWithoutAnotherCreatureOrArtifact() {
        Permanent vanguard = addReady(new VanguardOfTheRose());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(vanguard), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent vanguard = addReady(new VanguardOfTheRose());
        Permanent sacrificed = addReady(new GrizzlyBears());
        addMana();

        harness.activateAbility(player1, battlefieldIndex(vanguard), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
