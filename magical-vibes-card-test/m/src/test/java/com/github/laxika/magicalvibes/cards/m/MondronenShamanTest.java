package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.cards.v.VaultOfTheArchangel;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MondronenShaman.class, DawntreaderElk.class, SanctuaryCat.class, VaultOfTheArchangel.class, WitchbaneOrb.class})
class MondronenShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Tovolar's Magehunter when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new MondronenShaman());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(shaman.isTransformed()).isTrue();
        assertThat(shaman.getCard().getName()).isEqualTo("Tovolar's Magehunter");
    }

    @Test
    @DisplayName("Does not transform when any spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new MondronenShaman());

        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(shaman.isTransformed()).isFalse();
        assertThat(shaman.getCard().getName()).isEqualTo("Mondronen Shaman");
    }

    @Test
    @DisplayName("Tovolar's Magehunter transforms back when a player cast two or more spells last turn")
    void magehunterTransformsBackWhenTwoSpellsCast() {
        Permanent shaman = addTransformedMagehunter();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(shaman.isTransformed()).isFalse();
        assertThat(shaman.getCard().getName()).isEqualTo("Mondronen Shaman");
    }

    @Test
    @DisplayName("Tovolar's Magehunter does not transform back when each player cast only one spell")
    void magehunterDoesNotTransformBackWhenOnlyOneSpellEach() {
        Permanent shaman = addTransformedMagehunter();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(shaman.isTransformed()).isTrue();
        assertThat(shaman.getCard().getName()).isEqualTo("Tovolar's Magehunter");
    }

    @Test
    @DisplayName("Tovolar's Magehunter deals 2 damage to an opponent who casts a spell")
    void magehunterDamagesOpponentWhoCastsSpell() {
        addTransformedMagehunter();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DawntreaderElk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Tovolar's Magehunter does not trigger when its controller casts a spell")
    void magehunterDoesNotDamageControllerForOwnSpell() {
        addTransformedMagehunter();

        harness.setHand(player1, List.of(new SanctuaryCat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Magehunter damages a spell's caster even when that player has hexproof")
    void magehunterDamageDoesNotTarget() {
        addTransformedMagehunter();
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new DawntreaderElk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Magehunter's triggered damage applies lifelink granted by Vault of the Archangel")
    void magehunterDamageUsesGrantedLifelink() {
        addTransformedMagehunter();
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DawntreaderElk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Shaman also transforms at an opponent's upkeep")
    void transformsAtOpponentsUpkeep() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new MondronenShaman());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(shaman.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The front face does not damage an opponent for casting a spell")
    void frontFaceDoesNotDamageSpellCaster() {
        harness.addToBattlefield(player1, new MondronenShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new DawntreaderElk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Magehunter transforms at its controller's upkeep when that controller cast more than two spells")
    void magehunterTransformsForControllersSpellsAtOwnUpkeep() {
        Permanent shaman = addTransformedMagehunter();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(shaman.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Magehunter stays transformed when no spells were cast last turn")
    void magehunterStaysTransformedWhenNoSpellsWereCast() {
        Permanent shaman = addTransformedMagehunter();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(shaman.isTransformed()).isTrue();
    }

    private Permanent addTransformedMagehunter() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new MondronenShaman());
        shaman.setCard(shaman.getOriginalCard().getBackFaceCard());
        shaman.setTransformed(true);
        return shaman;
    }
}
