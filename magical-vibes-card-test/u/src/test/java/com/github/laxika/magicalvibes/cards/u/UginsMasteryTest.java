package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginsMastery.class, Ornithopter.class, GrizzlyBears.class, GiantGrowth.class})
class UginsMasteryTest extends BaseCardTest {

    @Test
    void manifestsTopCardWhenYouCastAColorlessCreature() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
    }

    @Test
    void doesNotManifestWhenYouCastAColoredCreature() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void mayTurnOneOfYourFaceDownCreaturesFaceUpWhenAttackingPowerIsAtLeastSix() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent otherFaceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherFaceDown.setFaceDownAsCloaked();
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(3, 4, 5));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, faceDown.getId());

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(otherFaceDown.isFaceDown()).isTrue();
    }

    @Test
    void doesNotTriggerWhenAttackingPowerIsLessThanSix() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2, 3));

        assertThat(gd.stack).isEmpty();
        assertThat(faceDown.isFaceDown()).isTrue();
    }

    @Test
    void doesNotManifestForAnOpponentsColorlessCreatureSpell() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    void doesNotManifestForAColorlessNoncreatureSpell() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new UginsMastery()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    void manifestWithAnEmptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canDeclineTurningACreatureFaceUp() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2, 3, 4));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(faceDown.isFaceDown()).isTrue();
    }

    @Test
    void attackTriggerDoesNotRecheckTotalPowerOnResolution() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2, 3, 4));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    void canTurnAManifestedEnchantmentFaceUpWithoutPayingItsManaCost() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new UginsMastery()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent faceDown = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(3, 4, 5));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDown);
    }

    @Test
    void cannotTurnAManifestedInstantFaceUp() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new GiantGrowth()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent faceDown = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(3, 4, 5));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isTrue();
        assertThat(faceDown.isManifested()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDown);
    }
}
