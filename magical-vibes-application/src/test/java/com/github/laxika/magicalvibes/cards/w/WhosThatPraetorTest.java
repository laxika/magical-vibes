package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EbonPraetor;
import com.github.laxika.magicalvibes.cards.e.EleshNornMotherOfMachines;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasProgressTyrant;
import com.github.laxika.magicalvibes.cards.s.SheoldredTheApocalypse;
import com.github.laxika.magicalvibes.cards.u.UrabraskHereticPraetor;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        WhosThatPraetor.class,
        EleshNornMotherOfMachines.class,
        JinGitaxiasProgressTyrant.class,
        SheoldredTheApocalypse.class,
        UrabraskHereticPraetor.class,
        VorinclexMonstrousRaider.class,
        EbonPraetor.class
})
class WhosThatPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one token copy of a Praetor")
    void createsTokenCopyOfRandomPraetor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new WhosThatPraetor(), "{6}");
        harness.passBothPriorities();

        List<Permanent> tokenCopies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokenCopies).hasSize(1);
    }
}
