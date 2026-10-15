package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SneakingGuide;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkoumWarrior.class, AkoumTeeth.class, SneakingGuide.class})
class AkoumWarriorTest extends BaseCardTest {

    @Test
    void creatureFaceEntersTheBattlefield() {
        harness.setHand(player1, List.of(new AkoumWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Akoum Warrior");
    }

    @Test
    void landFaceEntersTappedAndProducesRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AkoumWarrior()));

        harness.castCreature(player1, 0, 1);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(AkoumTeeth.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        addCreatureReady(player1, new AkoumWarrior());
        Permanent blocker = addCreatureReady(player2, new SneakingGuide());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 3));
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Sneaking Guide");
        harness.assertOnBattlefield(player1, "Akoum Warrior");
    }

    @Test
    void tappedLandFaceCannotProduceManaUntilUntapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AkoumWarrior()));
        harness.castCreature(player1, 0, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(findPermanent(player1, "Akoum Teeth").isTapped()).isTrue();
    }

    @Test
    void landFaceUsesTheTurnsLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AkoumWarrior(), new AkoumWarrior()));
        harness.castCreature(player1, 0, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Akoum Teeth")).isEqualTo(1);
        harness.assertInHand(player1, "Akoum Warrior");
    }
}
