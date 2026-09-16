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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtchingsOfTheChosen.class, GrizzlyBears.class, GoblinPiledriver.class})
class EtchingsOfTheChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Etchings of the Chosen enters applies its boost")
    void choosesCreatureTypeAndBoostsMatchingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinPiledriver());
        harness.setHand(player1, List.of(new EtchingsOfTheChosen()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
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

    private Permanent addEtchings(CardSubtype chosenSubtype) {
        Permanent etchings = harness.addToBattlefieldAndReturn(player1, new EtchingsOfTheChosen());
        etchings.setChosenSubtype(chosenSubtype);
        etchings.setSummoningSick(false);
        return etchings;
    }
}
