package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfessorOfZoomancy.class, Shock.class})
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

    @Test
    @DisplayName("Entering creates exactly one Pest without gaining life")
    void createsExactlyOnePest() {
        int lifeBefore = gd.getLife(player1.getId());

        castProfessorOfZoomancy();

        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
        assertThat(countPermanents(player2, "Pest")).isZero();
        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(pest.getEffectivePower()).isEqualTo(1);
        assertThat(pest.getEffectiveToughness()).isEqualTo(1);
        assertThat(pest.getEffectiveColors()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardColor.BLACK,
                com.github.laxika.magicalvibes.model.CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).containsExactly(
                com.github.laxika.magicalvibes.model.CardSubtype.PEST);
        assertThat(pest.getCard().getType()).isEqualTo(
                com.github.laxika.magicalvibes.model.CardType.CREATURE);
        assertThat(pest.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The Pest's death trigger works after Professor leaves")
    void pestDeathTriggerWorksWithoutProfessor() {
        castProfessorOfZoomancy();
        Permanent professor = findPermanent(player1, "Professor of Zoomancy");
        Permanent pest = findPermanent(player1, "Pest");
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.castInstant(player2, 0, professor.getId());
            resolveAllTriggers();
        }
        assertThat(countPermanents(player1, "Professor of Zoomancy")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, pest.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pest")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }
    private void castProfessorOfZoomancy() {
        harness.castFromHand(player1, new ProfessorOfZoomancy(), "{3}{G}");
        resolveAllTriggers();
    }
}
