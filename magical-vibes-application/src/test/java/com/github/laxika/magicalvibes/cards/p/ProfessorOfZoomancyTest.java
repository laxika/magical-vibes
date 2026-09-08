package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessorOfZoomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest token whose death gains 1 life")
    void createsPestWithDeathTrigger() {
        castProfessorOfZoomancy();

        Permanent pest = findPermanent(player1, "Pest");
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(pest.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private void castProfessorOfZoomancy() {
        harness.setHand(player1, List.of(new ProfessorOfZoomancy()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
