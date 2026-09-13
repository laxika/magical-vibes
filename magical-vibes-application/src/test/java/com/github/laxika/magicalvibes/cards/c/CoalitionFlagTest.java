package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.d.DragonArch;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.cards.s.Smash;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoalitionFlag.class, AngelfireCrusader.class, DegaDisciple.class, DragonArch.class,
        Jilt.class, Smash.class})
class CoalitionFlagTest extends BaseCardTest {

    @Test
    void enchantedCreatureBecomesFlagbearer() {
        Permanent creature = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(creature);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.FLAGBEARER);
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        Permanent ownCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent opponentCreature = addCreatureReady(player2, new AngelfireCrusader());

        harness.setHand(player1, List.of(new CoalitionFlag()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        harness.castEnchantment(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).contains(CardSubtype.FLAGBEARER);
    }

    @Test
    void opponentMustTargetFlagbearerWithJiltWhenAble() {
        Permanent flagbearer = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(flagbearer);
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");
        harness.castInstant(player2, 0, flagbearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentMustTargetFlagbearerWithActivatedAbilityWhenAble() {
        Permanent flagbearer = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(flagbearer);
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent disciple = addCreatureReady(player2, new DegaDisciple());

        int discipleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(disciple);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, discipleIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");
        harness.activateAbility(player2, discipleIndex, null, flagbearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerIsNotForcedToTargetItsOwnFlagbearer() {
        Permanent flagbearer = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(flagbearer);
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(otherCreature.getId());
    }

    @Test
    void opponentMustChooseAtLeastOneFlagbearerForKickedJilt() {
        Permanent flagbearer = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(flagbearer);
        Permanent firstOtherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent secondOtherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(
                player2, 0, firstOtherCreature.getId(), List.of(secondOtherCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castKickedInstantWithSacrifices(
                player2, 0, firstOtherCreature.getId(), List.of(flagbearer.getId()), List.of());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentMayTargetAnotherPermanentWhenNoFlagbearerIsAValidTarget() {
        Permanent flagbearer = addCreatureReady(player1, new AngelfireCrusader());
        attachFlag(flagbearer);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonArch());

        harness.setHand(player2, List.of(new Smash()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    void flagbearerGrantEndsWhenCoalitionFlagLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent aura = attachFlag(creature);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.FLAGBEARER);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).doesNotContain(CardSubtype.FLAGBEARER);
    }

    private Permanent attachFlag(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CoalitionFlag());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
