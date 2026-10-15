package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HelixPinnacle;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardOfBones.class, GrizzlyBears.class, Forest.class, HelixPinnacle.class, SongOfTheDryads.class})
class WardOfBonesTest extends BaseCardTest {

    // ===== Creature clause: opponent controlling more creatures can't cast creature spells =====

    @Test
    @DisplayName("Opponent controlling more creatures can't cast creature spells")
    void opponentWithMoreCreaturesCantCastCreatureSpells() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Player1 controls one creature, player2 (Ward's controller) controls none.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent with equal creature count can still cast creature spells")
    void opponentWithEqualCreaturesCanCastCreatureSpells() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Both players control one creature — not "more than", so no restriction.
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Ward of Bones controller is never restricted, even with more creatures")
    void controllerNotRestricted() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Controller (player2) has more creatures than the opponent — restriction is opponent-only.
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    // ===== Land clause: opponent controlling more lands can't play lands =====

    @Test
    @DisplayName("Opponent controlling more lands can't play lands")
    void opponentWithMoreLandsCantPlayLands() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Player1 controls one land, player2 controls none.
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent with equal land count can still play lands")
    void opponentWithEqualLandsCanPlayLands() {
        harness.addToBattlefield(player2, new WardOfBones());

        // Both players control one land — not "more than", so no restriction.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    void opponentWithMoreArtifactsCannotCastArtifacts() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player1, new WardOfBones());
        harness.addToBattlefield(player1, new WardOfBones());
        harness.setHand(player1, List.of(new WardOfBones()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThat(harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player1.getId()))
                .isEmpty();
        assertThatThrownBy(() -> harness.castFromHand(player1, new WardOfBones(), "{6}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void wardItselfCountsTowardEqualArtifactCount() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player1, new WardOfBones());

        harness.castFromHand(player1, new WardOfBones(), "{6}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void enchantmentRestrictionDoesNotRestrictCreatureSpells() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player1, new HelixPinnacle());
        harness.setHand(player1, List.of(new HelixPinnacle(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThat(harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player1.getId()))
                .containsExactly(1);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void equalEnchantmentCountAllowsEnchantmentSpells() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player2, new HelixPinnacle());
        harness.addToBattlefield(player1, new HelixPinnacle());

        harness.castFromHand(player1, new HelixPinnacle(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void creatureRestrictionIsEnforcedWhenCastingDirectly() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landRestrictionIsEnforcedWhenPlayingDirectly() {
        harness.addToBattlefield(player2, new WardOfBones());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void losingPrintedAbilitiesRemovesCreatureRestriction() {
        var ward = harness.addToBattlefieldAndReturn(player2, new WardOfBones());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, ward.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, ward)).isTrue();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears).hasSize(2);
    }

    @Test
    void losingPrintedAbilitiesRemovesLandRestriction() {
        var ward = harness.addToBattlefieldAndReturn(player2, new WardOfBones());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, ward.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, ward)).isTrue();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest).hasSize(3);
    }

    @Test
    void permanentsTurnedIntoLandsCountForLandRestriction() {
        harness.addToBattlefield(player2, new WardOfBones());
        var artifact = harness.addToBattlefieldAndReturn(player1, new WardOfBones());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, artifact)).isTrue();
        harness.setHand(player1, List.of(new Forest()));

        assertThat(harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player1.getId()))
                .isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
