package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FrontlineRebel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtharriSunsGlory.class, FrontlineRebel.class})
class OtharriSunsGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates one tapped and attacking Rebel for each experience counter")
    void attackingCreatesRebelsForExperienceCounters() {
        addCreatureReady(player1, new OtharriSunsGlory());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> rebels = findPermanents(player1, "Rebel");
        assertThat(rebels).hasSize(3);
        assertThat(rebels).allSatisfy(rebel -> {
            assertThat(rebel.isTapped()).isTrue();
            assertThat(rebel.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("The graveyard ability taps an untapped Rebel and returns Otharri tapped")
    void graveyardAbilityReturnsOtharri() {
        OtharriSunsGlory otharri = new OtharriSunsGlory();
        harness.setGraveyard(player1, List.of(otharri));
        Permanent rebel = addCreatureReady(player1, new FrontlineRebel());
        addOtharriMana();

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(rebel.isTapped()).isTrue();
        Permanent returned = findPermanent(player1, "Otharri, Suns' Glory");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability cannot activate without an untapped Rebel")
    void graveyardAbilityRequiresUntappedRebel() {
        harness.setGraveyard(player1, List.of(new OtharriSunsGlory()));
        addOtharriMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addOtharriMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
