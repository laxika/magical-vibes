package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GentlemansRise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HildibrandManderville.class, GentlemansRise.class})
class HildibrandMandervilleTest extends BaseCardTest {

    @Test
    void boostsOnlyCreatureTokensYouControl() {
        harness.addToBattlefield(player1, new HildibrandManderville());
        harness.addToBattlefield(player1, createCreature("Soldier", 1, 1, true));
        harness.addToBattlefield(player1, createCreature("Bear", 2, 2, false));
        harness.addToBattlefield(player2, createCreature("Goblin", 1, 1, true));

        Permanent ownToken = findPermanent(player1, "Soldier");
        Permanent ownCreature = findPermanent(player1, "Bear");
        Permanent opponentToken = findPermanent(player2, "Goblin");

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(1);
    }

    @Test
    void adventureCreatesZombieTokenAndExilesTheCard() {
        HildibrandManderville card = new HildibrandManderville();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void deathMayCastAdventureFromGraveyard() {
        HildibrandManderville card = new HildibrandManderville();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie").getCard().isToken()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void canCastCreatureFromExileAfterAdventureResolves() {
        HildibrandManderville card = new HildibrandManderville();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hildibrand Manderville");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    void adventurePermissionLastsThroughNextOwnTurn() {
        harness.setLibrary(player1, List.of(new HildibrandManderville()));
        harness.setLibrary(player2, List.of(new HildibrandManderville()));
        HildibrandManderville card = new HildibrandManderville();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void decliningToCastAtTriggerResolutionDoesNotPreventCastingLater() {
        HildibrandManderville card = new HildibrandManderville();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private Card createCreature(String name, int power, int toughness, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(token);
        return card;
    }
}
