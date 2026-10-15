package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiledriver;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({EtchingsOfTheChosen.class, GrizzlyBears.class, GoblinPiledriver.class})
class EtchingsOfTheChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Etchings of the Chosen enters applies its boost")
    void choosesCreatureTypeAndBoostsMatchingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinPiledriver());
        harness.castFromHand(player1, new EtchingsOfTheChosen(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        Permanent goblin = findPermanent(player1, "Goblin Piledriver");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a creature of the chosen type grants indestructible to a creature you control")
    void sacrificesChosenTypeAndGrantsIndestructible() {
        Permanent etchings = addEtchings(CardSubtype.BEAR);
        Permanent sacrificedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        sacrificedBear.setSummoningSick(false);
        targetGoblin.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etchings, targetGoblin);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedBear);
        assertThat(gqs.hasKeyword(gd, targetGoblin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A creature of another type cannot be sacrificed")
    void rejectsCreatureOfAnotherType() {
        Permanent etchings = addEtchings(CardSubtype.BEAR);
        Permanent sacrificeGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        Permanent targetGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        sacrificeGoblin.setSummoningSick(false);
        targetGoblin.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature of the chosen type");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etchings, sacrificeGoblin, targetGoblin);
        assertThat(gqs.hasKeyword(gd, targetGoblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addEtchings(CardSubtype.BEAR);
        Permanent sacrificedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        sacrificedBear.setSummoningSick(false);
        targetGoblin.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetGoblin.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, targetGoblin, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, targetGoblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void boostsLaterCreaturesButNotOpponentsCreatures() {
        addEtchings(CardSubtype.BEAR);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    void multipleEtchingsBoostMatchingCreaturesCumulatively() {
        addEtchings(CardSubtype.BEAR);
        addEtchings(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    void paysSacrificeBeforeResolutionEvenWithSummoningSickCreatures() {
        addEtchings(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, goblin.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotSacrificeOpponentsCreatureOfChosenType() {
        addEtchings(CardSubtype.BEAR);
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature of the chosen type");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBear);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        addEtchings(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiledriver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gqs.hasKeyword(gd, opposingGoblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent etchings = addEtchings(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, etchings.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear, etchings);
    }

    @Test
    void cannotActivateWithoutPayingMana() {
        addEtchings(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear, goblin);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addEtchings(CardSubtype chosenSubtype) {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new EtchingsOfTheChosen());
        etchings.setChosenSubtype(chosenSubtype);
        etchings.setSummoningSick(false);
        return etchings;
    }
}
