package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyboxFerryTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 2 animates Skybox Ferry until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent ferry = addReady(new SkyboxFerry());
        Permanent crew = addReady(new GrizzlyBears());

        activate(ferry);

        assertThat(gqs.isCreature(gd, ferry)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ferry)).isFalse();
    }

    @Test
    @DisplayName("Cycling Skybox Ferry discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SkyboxFerry()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skybox Ferry");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void activate(Permanent ferry) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ferry);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
