package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SageOfTheInwardEye.class, GrizzlyBears.class, Shock.class})
class SageOfTheInwardEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives your creatures lifelink until end of turn")
    void noncreatureSpellGrantsLifelink() {
        harness.addToBattlefield(player1, new SageOfTheInwardEye());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not grant lifelink")
    void creatureSpellDoesNotGrantLifelink() {
        harness.addToBattlefield(player1, new SageOfTheInwardEye());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SageOfTheInwardEye());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Sage gains lifelink from its own noncreature-spell trigger")
    void sageAlsoGainsLifelink() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfTheInwardEye());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, sage, Keyword.LIFELINK)).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not grant lifelink")
    void opponentSpellDoesNotGrantLifelink() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfTheInwardEye());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.hasKeyword(gd, sage, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink applies to creatures present at resolution, not later arrivals")
    void creaturesAreSelectedAtResolution() {
        harness.addToBattlefield(player1, new SageOfTheInwardEye());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();
    }
}
