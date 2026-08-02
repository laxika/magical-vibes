package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AltacBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creature dying gives +2/+0, first strike and haste")
    void pumpsWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Grizzly Bears dies
        harness.passBothPriorities(); // resolve first Bloodseeker trigger
        harness.passBothPriorities(); // resolve second Bloodseeker trigger

        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(2);
        assertThat(bloodseeker.getToughnessModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → player1's Bears dies

        assertThat(gd.stack).isEmpty();
        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Boost and keywords wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.passBothPriorities(); // CLEANUP -> next turn

        assertThat(bloodseeker.getPowerModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    private Permanent findBloodseeker() {
        return findPermanent(player1, "Altac Bloodseeker");
    }
}
