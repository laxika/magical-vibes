package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunCrestedPterodon.class, RaptorCompanion.class, SailorOfMeans.class})
class SunCrestedPterodonTest extends BaseCardTest {

    @Test
    void hasVigilanceWithAnotherDinosaur() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, new RaptorCompanion());


        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotHaveVigilanceWithoutAnotherDinosaur() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());


        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void nonDinosaurDoesNotGrantVigilance() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, new SailorOfMeans());


        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentDinosaurDoesNotGrantVigilance() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player2, new RaptorCompanion());


        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void losesVigilanceWhenTheOtherDinosaurLeaves() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, new RaptorCompanion());

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Raptor Companion"));

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void gainsVigilanceWhenAnotherDinosaurEnters() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();

        harness.addToBattlefield(player1, new RaptorCompanion());

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void anotherPterodonGrantsVigilanceToBoth() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());

        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void attackingWithAnotherDinosaurDoesNotTapPterodon() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, new RaptorCompanion());
        pterodon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(pterodon.isAttacking()).isTrue();
        assertThat(pterodon.isTapped()).isFalse();
    }

    @Test
    void attackingWithoutAnotherDinosaurTapsPterodon() {
        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        pterodon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(pterodon.isAttacking()).isTrue();
        assertThat(pterodon.isTapped()).isTrue();
    }
}
