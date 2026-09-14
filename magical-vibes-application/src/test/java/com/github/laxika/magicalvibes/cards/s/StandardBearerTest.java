package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.d.DragonArch;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandardBearer.class, AngelfireCrusader.class, DegaDisciple.class, DragonArch.class,
        Jilt.class, Smash.class})
class StandardBearerTest extends BaseCardTest {

    @Test
    void opponentMustTargetStandardBearerWhenAble() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castInstant(player2, 0, standardBearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerIsNotForcedToTargetStandardBearer() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(otherCreature.getId());
        assertThat(standardBearer.getId()).isNotEqualTo(otherCreature.getId());
    }

    @Test
    void opponentMustTargetStandardBearerWithActivatedAbilityWhenAble() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent disciple = addCreatureReady(player2, new DegaDisciple());

        int discipleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(disciple);

        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, discipleIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.activateAbility(player2, discipleIndex, null, standardBearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentMustChooseAtLeastOneFlagbearerForKickedSpell() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
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
                player2, 0, firstOtherCreature.getId(), List.of(standardBearer.getId()), List.of());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(firstOtherCreature.getId(), standardBearer.getId());
    }

    @Test
    void opponentMayTargetAnotherPermanentWhenNoFlagbearerIsAValidTarget() {
        addCreatureReady(player1, new StandardBearer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonArch());

        harness.setHand(player2, List.of(new Smash()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @CardUsed(WoodlandChangeling.class)
    void changelingCanSatisfyFlagbearerRequirement() {
        addCreatureReady(player1, new StandardBearer());
        Permanent changeling = addCreatureReady(player1, new WoodlandChangeling());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, changeling.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(changeling.getId());
    }
}
