package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TawnosUrzasApprentice.class, IcyManipulator.class, TheOzolith.class,
        GrizzlyBears.class, ProdigalPyromancer.class})
class TawnosUrzasApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an activated ability from an artifact source")
    void copiesArtifactActivatedAbility() {
        Permanent tawnos = addReadyTawnos();
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Icy Manipulator"), null, target.getId());
        UUID icyAbilityId = gd.stack.getLast().getCard().getId();

        harness.activateAbility(player1, findBattlefieldIndex(player1, "Tawnos, Urza's Apprentice"), null, icyAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(tawnos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Copies a triggered ability from an artifact source")
    void copiesArtifactTriggeredAbility() {
        addReadyTawnos();
        harness.addToBattlefield(player1, new TheOzolith());
        Permanent leavingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        leavingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, leavingCreature));
        UUID ozolithTriggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Tawnos, Urza's Apprentice"), null, ozolithTriggerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ozolith = findPermanent(player1, "The Ozolith");
        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an ability from a non-artifact source")
    void cannotTargetNonArtifactAbility() {
        addReadyTawnos();
        addReadyCreature(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Prodigal Pyromancer"), null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                findBattlefieldIndex(player1, "Tawnos, Urza's Apprentice"),
                null,
                pyromancerAbilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTawnos() {
        return addReadyCreature(player1, new TawnosUrzasApprentice());
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int findBattlefieldIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (name.equals(battlefield.get(i).getCard().getName())) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}
