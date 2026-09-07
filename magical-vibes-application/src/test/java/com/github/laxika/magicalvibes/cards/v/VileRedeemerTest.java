package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileRedeemer.class})
class VileRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {C} creates one Eldrazi Scion for each of your nontoken creature deaths")
    void payingColorlessCreatesScionsForOwnDeaths() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.nontokenCreatureDeathCountThisTurn.put(player2.getId(), 3);

        castVileRedeemer(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        assertThat(scions).hasSize(2);

        Permanent scion = scions.getFirst();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the optional payment creates no Eldrazi Scions")
    void decliningColorlessCreatesNoScions() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 2);

        castVileRedeemer(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    private void castVileRedeemer(int colorlessMana) {
        harness.setHand(player1, List.of(new VileRedeemer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castCreature(player1, 0);
    }
}
