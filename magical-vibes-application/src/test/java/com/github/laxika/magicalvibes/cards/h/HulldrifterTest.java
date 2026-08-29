package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HulldrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws two cards")
    void entersAndDrawsTwoCards() {
        harness.setHand(player1, List.of(new Hulldrifter()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Hulldrifter");
    }

    @Test
    @DisplayName("Crew 3 animates Hulldrifter and taps the crew")
    void crewAnimatesHulldrifterAndTapsCrew() {
        Permanent hulldrifter = addHulldrifterReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addHulldrifterReady(Player player) {
        Permanent permanent = new Permanent(new Hulldrifter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new HillGiant());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
