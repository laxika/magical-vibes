package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HelpingHand;
import com.github.laxika.magicalvibes.cards.m.MalametBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandMoonsSmithy.class, BarracksOfTheThousand.class, MalametBrawler.class, HelpingHand.class})
class ThousandMoonsSmithyTest extends BaseCardTest {

    @Test
    void createsGnomeWhosePowerAndToughnessTrackArtifactsAndCreatures() {
        addSmithyByCasting();

        Permanent gnome = findPermanents(player1, "Gnome Soldier").getFirst();
        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(2);

        harness.addToBattlefield(player1, new MalametBrawler());

        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(3);
    }

    @Test
    void mayTapFiveArtifactsOrCreaturesToTransformAtFirstMainPhase() {
        Permanent smithy = addSmithyByCasting();
        harness.addToBattlefield(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametBrawler());

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
        harness.setHand(player1, List.of(new MalametBrawler()));

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
        harness.setHand(player1, List.of(new MalametBrawler()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gnome Soldier")).isEmpty();
    }

    @Test
    void mayDeclineTransformationWithoutTappingAnything() {
        Permanent smithy = addSmithyByCasting();
        harness.addToBattlefield(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametBrawler());

        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(smithy.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
    }

    @Test
    void cannotTransformWithOnlyFourUntappedPermanents() {
        Permanent smithy = addSmithyByCasting();
        harness.addToBattlefield(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametBrawler());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        tappedCreature.tap();
        harness.addToBattlefield(player2, new MalametBrawler());

        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(smithy.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isTapped))
                .containsExactly(tappedCreature);
    }

    @Test
    void tokenCountsOnlyItsControllersPermanentsAndShrinksWhenOneLeaves() {
        Permanent smithy = addSmithyByCasting();
        Permanent gnome = findPermanent(player1, "Gnome Soldier");
        harness.addToBattlefield(player2, new MalametBrawler());

        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(smithy);
        gd.playerGraveyards.get(player1.getId()).add(smithy.getOriginalCard());

        assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(1);
    }

    @Test
    void backFaceTriggersForArtifactSpellBeforeThatSpellResolves() {
        Permanent barracks = addTransformedBarracks(player1);
        harness.activateAbility(player1, battlefieldIndex(barracks), 0, null, null);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new ThousandMoonsSmithy()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gnome Soldier")).isEqualTo(1);
        assertThat(findPermanents(player1, "Thousand Moons Smithy")).isEmpty();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gnome Soldier")).isEqualTo(2);
        for (Permanent gnome : findPermanents(player1, "Gnome Soldier")) {
            assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(4);
        }
    }

    @Test
    void canTransformWithoutTappingSmithyWhenFiveOtherPermanentsAreAvailable() {
        Permanent smithy = addSmithyByCasting();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new MalametBrawler());
        }
        List<Permanent> payment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p != smithy)
                .toList();

        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        for (Permanent permanent : payment) {
            harness.handlePermanentChosen(player1, permanent.getId());
        }

        assertThat(smithy.isTransformed()).isTrue();
        assertThat(smithy.isTapped()).isFalse();
        assertThat(payment).allMatch(Permanent::isTapped);
    }

    @Test
    void backFaceDoesNotTriggerForSorceryPaidWithItsMana() {
        Permanent barracks = addTransformedBarracks(player1);
        MalametBrawler creature = new MalametBrawler();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.activateAbility(player1, battlefieldIndex(barracks), 0, null, null);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanents(player1, "Gnome Soldier")).isEmpty();
        assertThat(findPermanents(player1, "Malamet Brawler")).hasSize(1);
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
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
