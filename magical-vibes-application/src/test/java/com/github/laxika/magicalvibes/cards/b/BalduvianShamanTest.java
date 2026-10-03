package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.e.EnergyStorm;
import com.github.laxika.magicalvibes.cards.e.EnduringRenewal;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianShaman.class, CircleOfProtectionBlack.class, EnergyStorm.class, EnduringRenewal.class,
        ZuranSpellcaster.class})
class BalduvianShamanTest extends BaseCardTest {

    @Test
    @DisplayName("The ability still changes text and grants upkeep after the Shaman dies")
    void abilityResolvesAfterShamanDies() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman), null, cop.getId());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster),
                null, shaman.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Balduvian Shaman");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(cop.getTextReplacements()).containsExactly(new TextReplacement("black", "blue"));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Circle of Protection: Black");
    }

    @Test
    @DisplayName("A pending activation cannot change an enchantment that has since gained cumulative upkeep")
    void pendingActivationChecksCumulativeUpkeepAtResolution() {
        Permanent first = addCreatureReady(player1, new BalduvianShaman());
        Permanent second = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(first), null, cop.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(second), null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(cop.getTextReplacements()).containsExactly(new TextReplacement("black", "blue"));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Circle of Protection: Black");
    }

    @Test
    @DisplayName("Granted cumulative upkeep costs one mana for each age counter")
    void grantedUpkeepCostIncreasesWithAgeCounters() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman), null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Circle of Protection: Black");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Circle of Protection: Black");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Shaman cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BalduvianShaman());
        shaman.setSummoningSick(true);
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(shaman), null, cop.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shaman.isTapped()).isFalse();
        assertThat(cop.hasCumulativeUpkeep()).isFalse();
    }

    @Test
    @DisplayName("Changes color word and grants cumulative upkeep {1}")
    void changesTextAndGrantsCumulativeUpkeep() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(cop.getTextReplacements()).containsExactly(new TextReplacement("black", "blue"));
        assertThat(cop.hasCumulativeUpkeep()).isTrue();
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Changed text affects the target enchantment's activated ability")
    void changedTextAffectsTargetActivatedAbility() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());
        Permanent blueSource = addCreatureReady(player2, new ZuranSpellcaster());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        int copIdx = gd.playerBattlefields.get(player1.getId()).indexOf(cop);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, copIdx, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, blueSource.getId());
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(blueSource.getId()));
    }

    @Test
    @DisplayName("Granted cumulative upkeep triggers and can be paid")
    void grantedCumulativeUpkeepTriggers() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cop);
    }

    @Test
    @DisplayName("Declining granted cumulative upkeep sacrifices the enchantment")
    void decliningGrantedCumulativeUpkeepSacrifices() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cop);
        harness.assertInGraveyard(player1, "Circle of Protection: Black");
    }

    @Test
    @DisplayName("Cannot target an enchantment that already has cumulative upkeep")
    void cannotTargetEnchantmentWithCumulativeUpkeep() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, storm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot retarget the same enchantment after granting cumulative upkeep")
    void cannotRetargetAfterGrant() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        shaman.untap();
        shaman.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, cop.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's white enchantment")
    void cannotTargetOpponentsEnchantment() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, cop.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a white enchantment without a color word")
    void canTargetWhiteEnchantmentWithoutColorWord() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent renewal = harness.addToBattlefieldAndReturn(player1, new EnduringRenewal());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, renewal.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(renewal.getTextReplacements()).containsExactly(new TextReplacement("black", "blue"));
        assertThat(renewal.hasCumulativeUpkeep()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonwhite nonenchantment permanent")
    void cannotTargetNonWhiteNonEnchantment() {
        Permanent shaman = addCreatureReady(player1, new BalduvianShaman());
        Permanent creature = addCreatureReady(player1, new ZuranSpellcaster());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
