package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.g.GoblinFurrier;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OhranYeti.class, BorealDruid.class, GoblinFurrier.class, SnowCoveredMountain.class})
class OhranYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana ability grants first strike to a snow creature")
    void grantsFirstStrikeToSnowCreature() {
        addCreatureReady(player1, new OhranYeti());
        Permanent snowCreature = addCreatureReady(player1, new BorealDruid());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, snowCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new OhranYeti());
        Permanent snowCreature = addCreatureReady(player1, new BorealDruid());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, snowCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A nonsnow creature cannot be targeted")
    void rejectsNonsnowCreatureTarget() {
        addCreatureReady(player1, new OhranYeti());
        Permanent creature = addCreatureReady(player1, new GoblinFurrier());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a snow creature");
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new OhranYeti());
        Permanent snowCreature = addCreatureReady(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, snowCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A snow noncreature cannot be targeted")
    void rejectsSnowNoncreatureTarget() {
        addCreatureReady(player1, new OhranYeti());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a snow creature");
    }

    @Test
    @DisplayName("The ability can target an opponent's snow creature")
    void grantsFirstStrikeToOpponentsSnowCreature() {
        addCreatureReady(player1, new OhranYeti());
        Permanent snowCreature = addCreatureReady(player2, new BorealDruid());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, snowCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick tapped Yeti can grant itself first strike")
    void summoningSickTappedYetiCanTargetItself() {
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new OhranYeti());
        yeti.setSummoningSick(true);
        yeti.setTapped(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, yeti.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, yeti, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(yeti.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana from a snow land pays the snow activation cost")
    void snowMountainManaPaysSnowCost() {
        Permanent yeti = addCreatureReady(player1, new OhranYeti());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.tapPermanent(player1, 1);

        harness.activateAbility(player1, 0, 0, yeti.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, yeti, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent yeti = addCreatureReady(player1, new OhranYeti());
        Permanent snowCreature = addCreatureReady(player1, new BorealDruid());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, snowCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(yeti);
        gd.playerGraveyards.get(player1.getId()).add(yeti.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }
}
