package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TeferisProtection;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
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

@CardUsed({Oubliette.class, Disenchant.class, GrizzlyBears.class, Island.class, UnholyStrength.class,
        TeferisProtection.class})
class OublietteTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability phases out the target creature until Oubliette leaves")
    void phasesOutTargetUntilOublietteLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOubliette(creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        harness.performUntapStep(player2);

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("The creature phases in tapped when Oubliette leaves")
    void phasesInTappedWhenOublietteLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOubliette(creature.getId());
        UUID oublietteId = harness.getPermanentId(player1, "Oubliette");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, oublietteId);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Oubliette");
    }

    @Test
    @DisplayName("Oubliette cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Oubliette()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolveOubliette(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Oubliette()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Removing Oubliette before its enter trigger resolves does not phase out or tap the creature")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Oubliette(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Oubliette"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Oubliette");
    }

    @Test
    @DisplayName("An opposing Aura phases out and returns attached with the creature without being tapped")
    void opposingAuraPhasesWithCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID auraId = harness.getPermanentId(player1, "Unholy Strength");
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(auraId)).findFirst().orElseThrow();

        castAndResolveOubliette(creature.getId());
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Oubliette"));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(aura.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Phasing out Oubliette before its enter trigger resolves does not stop that trigger")
    void phasedOutSourceStillPhasesOutTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Oubliette(), new TeferisProtection()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }
}
