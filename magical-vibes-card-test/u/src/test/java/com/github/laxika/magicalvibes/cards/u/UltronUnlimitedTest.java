package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronUnlimited.class, HypnoticGrifter.class, Mountain.class, Murder.class})
class UltronUnlimitedTest extends BaseCardTest {

    @Test
    void attackingConnivesAndPayingCreatesRobotVillainToken() {
        Permanent ultron = addCreatureReady(player1, new UltronUnlimited());
        harness.setHand(player1, List.of(new UltronUnlimited()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ultron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent token = findPermanents(player1, "Robot Villain Token").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ROBOT, CardSubtype.VILLAIN);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void conniveByAnotherControlledCreatureCanBeDeclined() {
        addCreatureReady(player1, new UltronUnlimited());
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new UltronUnlimited()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Robot Villain Token"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void discardingLandStillAllowsTokenCreationWithoutAddingCounter() {
        Permanent ultron = addCreatureReady(player1, new UltronUnlimited());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new HypnoticGrifter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ultron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Robot Villain Token")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Hypnotic Grifter");
    }

    @Test
    void opponentsCreatureConnivingDoesNotTriggerUltron() {
        addCreatureReady(player1, new UltronUnlimited());
        Permanent grifter = addCreatureReady(player2, new HypnoticGrifter());
        harness.setHand(player2, List.of(new UltronUnlimited()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Robot Villain Token")).isZero();
        assertThat(countPermanents(player2, "Robot Villain Token")).isZero();
    }

    @Test
    void controlledCreatureStillConnivesAfterLeavingBattlefield() {
        addCreatureReady(player1, new UltronUnlimited());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new UltronUnlimited()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.castAndResolveInstant(player2, 0, grifter.getId());
        harness.assertInGraveyard(player1, "Hypnotic Grifter");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Robot Villain Token")).isEqualTo(1);
    }
}
