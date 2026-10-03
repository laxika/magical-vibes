package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FoundryStreetDenizen;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdsFavor.class, RuneclawBear.class, FoundryStreetDenizen.class})
class CrowdsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crowd's Favor gives +1/+0 and first strike to target creature")
    void resolvingBoostsAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CrowdsFavor()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Convoke taps an untapped red creature to pay the mana cost")
    void convokePaysCost() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        harness.setHand(player1, List.of(new CrowdsFavor()));

        UUID targetId = harness.getPermanentId(player1, "Runeclaw Bear");
        UUID convokeId = harness.getPermanentId(player1, "Foundry Street Denizen");

        harness.castInstantWithConvoke(player1, 0, List.of(targetId), List.of(convokeId));
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        Permanent goblin = findPermanent(player1, "Foundry Street Denizen");
        assertThat(goblin.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CrowdsFavor()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Crowd's Favor fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CrowdsFavor()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Crowd's Favor");
    }

    @Test
    @DisplayName("Crowd's Favor can boost a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new CrowdsFavor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Crowd's Favor");
    }

    @Test
    @DisplayName("A summoning-sick target can convoke Crowd's Favor itself")
    void targetCanConvokeWhileSummoningSick() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new CrowdsFavor()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Crowd's Favor");
    }

    @Test
    @DisplayName("A green creature cannot convoke the red mana cost")
    void greenCreatureCannotPayRedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CrowdsFavor()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Crowd's Favor");
    }

    @Test
    @DisplayName("An already tapped creature cannot convoke Crowd's Favor")
    void tappedCreatureCannotConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());
        target.setTapped(true);
        harness.setHand(player1, List.of(new CrowdsFavor()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Crowd's Favor");
    }
}
