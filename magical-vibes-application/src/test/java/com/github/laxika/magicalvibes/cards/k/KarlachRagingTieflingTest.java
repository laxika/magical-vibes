package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarlachRagingTiefling.class, GrizzlyBears.class})
class KarlachRagingTieflingTest extends BaseCardTest {

    @Test
    void whiteSpecializationCreatesAKnightAndBoostsYourCreatures() {
        Permanent karlach = addCreatureReady(player1, new KarlachRagingTiefling());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        assertThat(karlach.getCard().getActivatedAbilities()).hasSize(5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(karlach.getCard().getName()).isEqualTo("Karlach, Tiefling Zealot");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Knight"))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, karlach)).isEqualTo(5);
    }

    @Test
    void graveyardSpecializationReturnsKarlachToTheBattlefield() {
        harness.setGraveyard(player1, List.of(new KarlachRagingTiefling()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent karlach = findPermanent(player1, "Karlach, Tiefling Zealot");
        assertThat(karlach).isNotNull();
        harness.assertNotInGraveyard(player1, "Karlach, Raging Tiefling");
    }
}
