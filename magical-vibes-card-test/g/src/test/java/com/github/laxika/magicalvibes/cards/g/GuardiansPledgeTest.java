package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardiansPledge.class, EliteVanguard.class, GarruksCompanion.class})
class GuardiansPledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts white creatures you control by +2/+2")
    void boostsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(2);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-white creatures you control")
    void doesNotBoostNonWhiteCreatures() {
        harness.addToBattlefield(player1, new GarruksCompanion());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent companion = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(companion.getPowerModifier()).isEqualTo(0);
        assertThat(companion.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost opponent's white creatures")
    void doesNotBoostOpponentWhiteCreatures() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent vanguard = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(0);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(0);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boosts every white creature present when the spell resolves")
    void determinesAffectedCreaturesAtResolution() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(permanent -> {
            assertThat(permanent.getPowerModifier()).isEqualTo(2);
            assertThat(permanent.getToughnessModifier()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Does not boost white creatures entering after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.addToBattlefield(player1, new EliteVanguard());

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.getFirst().getPowerModifier()).isEqualTo(2);
        assertThat(battlefield.getFirst().getToughnessModifier()).isEqualTo(2);
        assertThat(battlefield.getLast().getPowerModifier()).isZero();
        assertThat(battlefield.getLast().getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Resolves without any creatures or targets")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Guardians' Pledge");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
