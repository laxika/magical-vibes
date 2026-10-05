package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheScroll.class, FathomSeer.class})
class MagusOfTheScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the controller to name a card")
    void resolvingPromptsController() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheScroll());
        Permanent seer = harness.addToBattlefieldAndReturn(player2, new FathomSeer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));

        harness.activateAbility(player1, 0, null, seer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.context())
                .isInstanceOf(ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice.class);
        var context = (ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice) interaction.context();
        assertThat(context.targetId()).isEqualTo(seer.getId());
        assertThat(context.sourcePermanentId()).isEqualTo(magus.getId());
        assertThat(interaction.options()).contains("Magus of the Scroll", "Fathom Seer");
        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A matching reveal deals 2 damage to the target player")
    void matchingRevealDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Magus of the Scroll");

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonmatching reveal deals no damage")
    void mismatchedRevealDealsNoDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        harness.setHand(player2, List.of(new FathomSeer()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Fathom Seer");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A matching reveal can deal 2 damage to a target creature")
    void matchingRevealDamagesTargetCreature() {
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        Permanent seer = harness.addToBattlefieldAndReturn(player2, new FathomSeer());

        harness.activateAbility(player1, 0, null, seer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Magus of the Scroll");

        assertThat(seer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand reveals nothing and deals no damage")
    void emptyHandDealsNoDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Magus of the Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The random reveal comes from the ability controller's hand")
    void revealUsesControllerHand() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        harness.setHand(player2, List.of(new FathomSeer()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Fathom Seer");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal target makes the ability fizzle before the name choice")
    void illegalTargetFizzlesBeforeNameChoice() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        Permanent seer = harness.addToBattlefieldAndReturn(player2, new FathomSeer());

        harness.activateAbility(player1, 0, null, seer.getId());
        gd.playerBattlefields.get(player2.getId()).remove(seer);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("The name prompt offers real card names absent from the game")
    void canChooseNameAbsentFromGame() {
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        harness.setHand(player2, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.options()).contains("Fathom Seer");
        harness.handleListChoice(player1, "Fathom Seer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Name choices do not disclose changes in the opponent's hidden hand")
    void nameChoicesAreIndependentOfOpponentHand() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheScroll());
        harness.setHand(player1, List.of(new MagusOfTheScroll()));
        harness.setHand(player2, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        List<String> firstOptions = List.copyOf(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Magus of the Scroll");

        harness.setHand(player2, List.of(new FathomSeer()));
        magus.setTapped(false);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.options()).containsExactlyElementsOf(firstOptions);
        harness.handleListChoice(player1, "Magus of the Scroll");
    }

    @Test
    @DisplayName("The ability still deals damage after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(magus);
        gd.playerGraveyards.get(player1.getId()).add(magus.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Magus of the Scroll");

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The random reveal uses the hand as it exists at resolution")
    void revealUsesHandAtResolution() {
        addCreatureReady(player1, new MagusOfTheScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new MagusOfTheScroll()));

        harness.activateAbility(player1, 0, null, player2.getId());
        FathomSeer seer = new FathomSeer();
        harness.setHand(player1, List.of(seer));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Fathom Seer");

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(seer);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
