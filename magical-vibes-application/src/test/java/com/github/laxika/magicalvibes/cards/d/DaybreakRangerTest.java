package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaybreakRanger.class, AbbeyGriffin.class, DarkthicketWolf.class})
class DaybreakRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 2 damage to target creature with flying")
    void tapAbilityDeals2DamageToFlyingCreature() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new AbbeyGriffin());
        Permanent pegasus = findPermanent(player2, "Abbey Griffin");

        int rangerIdx = indexOf(player1, ranger);
        harness.activateAbility(player1, rangerIdx, null, pegasus.getId());
        harness.passBothPriorities();

        // Abbey Griffin is 2/2, 2 damage kills it
        harness.assertNotOnBattlefield(player2, "Abbey Griffin");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new DarkthicketWolf());
        Permanent bear = findPermanent(player2, "Darkthicket Wolf");

        int rangerIdx = indexOf(player1, ranger);

        assertThatThrownBy(() -> harness.activateAbility(player1, rangerIdx, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforms to Nightfall Predator when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(ranger.isTransformed()).isFalse();
        assertThat(ranger.getCard().getName()).isEqualTo("Daybreak Ranger");
    }

    @Test
    @DisplayName("Nightfall Predator transforms back when a player cast two or more spells last turn")
    void nightfallTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Transform to Nightfall Predator first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(ranger.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(ranger.isTransformed()).isFalse();
        assertThat(ranger.getCard().getName()).isEqualTo("Daybreak Ranger");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightfall Predator does not transform back when only one spell was cast last turn")
    void nightfallDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Transform to Nightfall Predator first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ranger.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
    }

    @Test
    @DisplayName("Nightfall Predator fights target creature")
    void nightfallPredatorFightsTargetCreature() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new DarkthicketWolf());
        Permanent bear = findPermanent(player2, "Darkthicket Wolf");

        // Transform to Nightfall Predator
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ranger.isTransformed()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);

        int rangerIdx = indexOf(player1, ranger);
        harness.activateAbility(player1, rangerIdx, null, bear.getId());
        harness.passBothPriorities();

        // Nightfall Predator is 4/4, Darkthicket Wolf is 2/2
        // Bear takes 4 damage → dies. Predator takes 2 damage → survives (4 toughness - 2 = 2 left).
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");
        harness.assertOnBattlefield(player1, "Nightfall Predator");
        assertThat(ranger.getMarkedDamage()).isEqualTo(2);
        assertThat(ranger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
    }

    @Test
    void flyingRestrictionIsCheckedAgainOnResolution() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());

        harness.activateAbility(player1, indexOf(player1, ranger), null, griffin.getId());
        griffin.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Abbey Griffin");
        assertThat(griffin.getMarkedDamage()).isZero();
        assertThat(ranger.isTapped()).isTrue();
    }

    @Test
    void cannotActivateTapAbilityWhileSummoningSick() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ranger), null, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ranger.isTapped()).isFalse();
    }

    @Test
    void fightRequiresRedMana() {
        Permanent predator = transformReadyRanger();
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, predator), null, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(predator.isTapped()).isFalse();
    }

    @Test
    void fightDealsNoDamageWhenSourceLeavesBeforeResolution() {
        Permanent predator = transformReadyRanger();
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, predator), null, wolf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(predator);
        gd.playerGraveyards.get(player1.getId()).add(predator.getOriginalCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darkthicket Wolf");
        assertThat(wolf.getMarkedDamage()).isZero();
    }

    @Test
    void predatorCanFightItself() {
        Permanent predator = transformReadyRanger();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, predator), null, predator.getId());
        harness.passBothPriorities();

        assertThat(predator.getMarkedDamage()).isEqualTo(8);
        harness.assertNotOnBattlefield(player1, "Nightfall Predator");
    }

    @Test
    void fightUsesPowerAtResolutionAndBothCreaturesDealLethalDamage() {
        Permanent predator = transformReadyRanger();
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.activateAbility(player1, indexOf(player1, predator), null, wolf.getId());
        harness.activateAbility(player2, indexOf(player2, wolf), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightfall Predator");
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");
    }

    @Test
    void fightDamageRemainsWhenTransformingBack() {
        Permanent predator = transformReadyRanger();
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, predator), null, wolf.getId());
        harness.passBothPriorities();
        assertThat(predator.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Daybreak Ranger");
        harness.assertNotOnBattlefield(player1, "Nightfall Predator");
    }

    private Permanent transformReadyRanger() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ranger.isTransformed()).isTrue();
        return ranger;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
