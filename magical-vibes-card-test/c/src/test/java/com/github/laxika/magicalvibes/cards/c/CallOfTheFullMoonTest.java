package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallOfTheFullMoon.class, TimberpackWolf.class, Mountain.class})
class CallOfTheFullMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+2 and has trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());

        castAuraOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Sacrificed at upkeep when a player cast two or more spells last turn")
    void sacrificedWhenTwoSpellsCastLastTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        castAuraOn(bears);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        runUpkeep();
        harness.passBothPriorities(); // resolve the sacrifice trigger

        harness.assertNotOnBattlefield(player1, "Call of the Full Moon");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Not sacrificed when each player cast at most one spell last turn")
    void staysWhenOnlyOneSpellCastLastTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        castAuraOn(bears);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        runUpkeep();

        harness.assertOnBattlefield(player1, "Call of the Full Moon");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("No upkeep trigger when no spells were cast last turn")
    void noTriggerWhenNoSpellsCastLastTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        castAuraOn(creature);
        gd.spellsCastLastTurn.clear();

        runUpkeep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Call of the Full Moon");
    }

    @Test
    @DisplayName("The sacrifice also triggers on the Aura controller's upkeep")
    void sacrificedOnControllersUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        castAuraOn(creature);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Call of the Full Moon");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Call of the Full Moon");
        harness.assertNotOnBattlefield(player1, "Call of the Full Moon");
        harness.assertOnBattlefield(player1, "Timberpack Wolf");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and sacrifices only the Aura")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        castAuraOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        runUpkeep();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Call of the Full Moon");
        harness.assertOnBattlefield(player2, "Timberpack Wolf");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counts a spell cast before the Aura and the Aura itself on the previous turn")
    void countsSpellsCastBeforeAuraEnteredBattlefield() {
        harness.castFromHand(player1, new TimberpackWolf(), "{1}{G}");
        harness.passBothPriorities();
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        castAuraOn(creature);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Call of the Full Moon");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call of the Full Moon");
        harness.assertInGraveyard(player1, "Call of the Full Moon");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting only the Aura last turn does not cause sacrifice")
    void survivesWhenOnlyAuraWasCast() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        castAuraOn(creature);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Call of the Full Moon");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @CardUsed({CallOfTheFullMoon.class, Mountain.class})
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new CallOfTheFullMoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Call of the Full Moon");
        assertThat(gd.stack).isEmpty();
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new CallOfTheFullMoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void runUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
    }
}
