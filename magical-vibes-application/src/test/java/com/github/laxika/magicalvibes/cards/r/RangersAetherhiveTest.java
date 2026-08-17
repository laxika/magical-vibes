package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AfterburnerExpert;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rangers' Aetherhive")
class RangersAetherhiveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating an exhaust ability creates a Thopter")
    void activatingExhaustAbilityCreatesThopter() {
        addReadyAetherhive();
        Permanent expert = harness.addToBattlefieldAndReturn(player1, new AfterburnerExpert());
        expert.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(findPermanents(player1, "Thopter").getFirst().getCard().getKeywords())
                .contains(Keyword.FLYING);
        assertThat(findPermanents(player1, "Thopter").getFirst().getCard().getAdditionalTypes())
                .contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crew does not trigger Thopter creation")
    void crewDoesNotTriggerThopterCreation() {
        Permanent aetherhive = addReadyAetherhive();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(gqs.isCreature(gd, aetherhive)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addReadyAetherhive() {
        Permanent aetherhive = harness.addToBattlefieldAndReturn(player1, new RangersAetherhive());
        aetherhive.setSummoningSick(false);
        return aetherhive;
    }
}
