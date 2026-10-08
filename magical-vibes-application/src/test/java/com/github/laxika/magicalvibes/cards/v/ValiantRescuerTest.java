package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantRescuer.class, DrannithHealer.class, CatharticReunion.class})
class ValiantRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("The first cycling each turn creates a 1/1 Human Soldier")
    void firstCycleEachTurnCreatesHumanSoldier() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second cycling in the same turn does not create another token")
    void secondCycleSameTurnDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new DrannithHealer(), new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer(), new ValiantRescuer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Drannith Healer");
    }
    @Test
    void cyclingRescuerFromHandDrawsWithoutCreatingItsOwnToken() {
        ValiantRescuer rescuer = new ValiantRescuer();
        DrannithHealer drawn = new DrannithHealer();
        harness.setHand(player1, List.of(rescuer));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rescuer);
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    void cyclingAnotherRescuerTriggersEachBattlefieldCopy() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new ValiantRescuer()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    void opponentCyclingDoesNotConsumeControllersFirstCycling() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player2, List.of(new DrannithHealer()));
        harness.setLibrary(player2, List.of(new ValiantRescuer()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isZero();

        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }

    @Test
    void cyclingBeforeRescuerEntersPreventsTriggerOnLaterCycling() {
        harness.setHand(player1, List.of(new DrannithHealer(), new ValiantRescuer(), new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer(), new ValiantRescuer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Valiant Rescuer");
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    void firstCyclingOnOpponentsTurnCreatesAnotherToken() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new DrannithHealer(), new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer(), new ValiantRescuer()));
        harness.setLibrary(player2, List.of(new ValiantRescuer(), new ValiantRescuer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    void ordinaryDiscardDoesNotTriggerOrConsumeFirstCycling() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new CatharticReunion(), new DrannithHealer(), new ValiantRescuer(), new DrannithHealer()));
        harness.setLibrary(player1, List.of(new ValiantRescuer(), new ValiantRescuer(), new ValiantRescuer(), new ValiantRescuer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, (java.util.UUID) null, List.of(1, 2));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }
}
