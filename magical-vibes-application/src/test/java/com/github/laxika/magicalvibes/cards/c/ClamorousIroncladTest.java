package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClamorousIroncladTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Clamorous Ironclad and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ClamorousIronclad()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Clamorous Ironclad");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Crew 3 animates Clamorous Ironclad and taps the crew")
    void crewAnimatesIroncladAndTapsCrew() {
        Permanent ironclad = new Permanent(new ClamorousIronclad());
        ironclad.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ironclad);

        Permanent crew = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        crew.setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ironclad), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ironclad)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }
}
