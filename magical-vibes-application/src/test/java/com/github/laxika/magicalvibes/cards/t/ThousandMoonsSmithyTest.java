package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandMoonsSmithy.class, BarracksOfTheThousand.class, GrizzlyBears.class})
class ThousandMoonsSmithyTest extends BaseCardTest {

    @Test
    void createsGnomeWhosePowerAndToughnessTrackArtifactsAndCreatures() {
        addSmithyByCasting();

        Permanent gnome = findPermanents(player1, "Gnome Soldier").getFirst();
        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(3);
    }

    @Test
    void mayTapFiveArtifactsOrCreaturesToTransformAtFirstMainPhase() {
        Permanent smithy = addSmithyByCasting();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(smithy.isTransformed()).isTrue();
        assertThat(smithy.getCard().getName()).isEqualTo("Barracks of the Thousand");
        assertThat(smithy.isTapped()).isTrue();
    }

    @Test
    void backFaceCreatesGnomeWhenItsManaCastsArtifactOrCreature() {
        Permanent barracks = addTransformedBarracks(player1);
        harness.activateAbility(player1, battlefieldIndex(barracks), 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent gnome = findPermanents(player1, "Gnome Soldier").getFirst();
        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(3);
    }

    @Test
    void backFaceDoesNotTriggerWhenItsManaIsNotUsed() {
        addTransformedBarracks(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gnome Soldier")).isEmpty();
    }

    private Permanent addSmithyByCasting() {
        harness.setHand(player1, List.of(new ThousandMoonsSmithy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Thousand Moons Smithy");
    }

    private Permanent addTransformedBarracks(Player player) {
        ThousandMoonsSmithy card = new ThousandMoonsSmithy();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
