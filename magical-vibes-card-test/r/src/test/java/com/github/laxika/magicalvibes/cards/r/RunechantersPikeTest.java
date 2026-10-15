package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunechantersPike.class, WalkingCorpse.class, ThinkTwice.class, BumpInTheNight.class})
class RunechantersPikeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gains first strike")
    void equippedCreatureGainsFirstStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not gain first strike from Pike")
    void unequippedCreatureNoFirstStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.addToBattlefield(player1, new RunechantersPike());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No boost with empty graveyard")
    void noBoostWithEmptyGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts instant cards in controller's graveyard")
    void boostCountsInstants() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());

        // 2 base + 2 instants = 4 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts sorcery cards in controller's graveyard")
    void boostCountsSorceries() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        gd.playerGraveyards.get(player1.getId()).add(new BumpInTheNight());

        // 2 base + 1 sorcery = 3 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts both instants and sorceries")
    void boostCountsBothInstantsAndSorceries() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player1.getId()).add(new BumpInTheNight());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());

        // 2 base + 2 instants + 1 sorcery = 5 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature cards in graveyard do not count")
    void creatureCardsDoNotCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        gd.playerGraveyards.get(player1.getId()).add(new WalkingCorpse());
        gd.playerGraveyards.get(player1.getId()).add(new WalkingCorpse());

        // No boost from creature cards
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count opponent's graveyard")
    void doesNotCountOpponentGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        // Only opponent has instants/sorceries in graveyard
        gd.playerGraveyards.get(player2.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player2.getId()).add(new BumpInTheNight());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts equipment controller's graveyard, not equipped creature's controller's")
    void countsEquipmentControllersGraveyard() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        // Pike controlled by player1, attached to player2's creature
        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(opponentBears.getId());

        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player2.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player2.getId()).add(new ThinkTwice());
        gd.playerGraveyards.get(player2.getId()).add(new ThinkTwice());

        // Should count player1's 2 instants, not player2's 3
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4); // 2 base + 2 instants
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost updates dynamically as graveyard changes")
    void boostUpdatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new BumpInTheNight());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        // Remove all cards from graveyard
        gd.playerGraveyards.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipping to another creature transfers boost and first strike")
    void equipTransfersBoostandFirstStrike() {
        Permanent bears1 = addCreatureReady(player1, new WalkingCorpse());

        Permanent bears2 = addCreatureReady(player1, new WalkingCorpse());

        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(bears1.getId());

        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());

        // bears1 has boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears1)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears1, Keyword.FIRST_STRIKE)).isTrue();

        // Move pike to bears2
        pike.setAttachedTo(bears2.getId());

        // bears1 loses boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears1, Keyword.FIRST_STRIKE)).isFalse();

        // bears2 gains boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears2)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears2, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip costs two mana and attaches Pike through the stack")
    void equipAttachesThroughStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());

        assertThat(pike.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(pike.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing Pike removes both continuous bonuses")
    void removingPikeRemovesBonuses() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        pike.setAttachedTo(creature.getId());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pike);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        harness.addToBattlefield(player1, new RunechantersPike());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated outside sorcery timing")
    void equipRejectsCombatTiming() {
        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pike.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An equip target leaving before resolution leaves the original attachment intact")
    void vanishedEquipTargetKeepsOriginalAttachment() {
        Permanent pike = harness.addToBattlefieldAndReturn(player1, new RunechantersPike());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        pike.setAttachedTo(original.getId());
        gd.playerGraveyards.get(player1.getId()).add(new ThinkTwice());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(pike.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
