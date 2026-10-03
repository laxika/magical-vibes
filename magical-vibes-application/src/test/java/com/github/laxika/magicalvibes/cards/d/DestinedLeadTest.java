package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestinedLead.class, Forest.class, PouncingCheetah.class})
class DestinedLeadTest extends BaseCardTest {

    @Test
    @DisplayName("Destined gives +1/+0 and indestructible until end of turn")
    void destinedPumpsAndGrantsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setHand(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Destined");
    }

    @Test
    @DisplayName("Destined boost and indestructible wear off at end of turn")
    void destinedEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setHand(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Destined cannot target a non-creature")
    void destinedCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lead from graveyard sets must-be-blocked-by-all, then exiles")
    void leadFlashbackSetsFlagAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setGraveyard(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Destined") || c.getName().equals("Lead"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Destined"));
    }

    @Test
    @DisplayName("Lead flag wears off at end of turn")
    void leadFlagWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setGraveyard(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Lead cannot target a non-creature")
    void leadCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Lead requires sorcery timing")
    void leadRequiresSorceryTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setGraveyard(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Destined can protect an opponent's creature")
    void destinedCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PouncingCheetah());
        harness.setHand(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Lead forces every able blocker to block, but does not force tapped creatures")
    void leadForcesAllAbleBlockers() {
        Permanent target = addCreatureReady(player1, new PouncingCheetah());
        Permanent firstBlocker = addCreatureReady(player2, new PouncingCheetah());
        Permanent secondBlocker = addCreatureReady(player2, new PouncingCheetah());
        Permanent tappedBlocker = addCreatureReady(player2, new PouncingCheetah());
        tappedBlocker.tap();
        harness.setGraveyard(player1, List.of(new DestinedLead()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.getBlockingTargetIds()).containsExactly(target.getId());
        assertThat(secondBlocker.getBlockingTargetIds()).containsExactly(target.getId());
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Lead is exiled even when its target leaves before resolution")
    void leadExilesWhenTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        DestinedLead spell = new DestinedLead();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }
}
