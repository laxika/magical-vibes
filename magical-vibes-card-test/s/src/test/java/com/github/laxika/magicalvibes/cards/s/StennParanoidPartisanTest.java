package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StennParanoidPartisan.class, Divination.class, GrizzlyBears.class})
class StennParanoidPartisanTest extends BaseCardTest {

    @Test
    void choosesNoncreatureNonlandCardType() {
        harness.setHand(player1, List.of(new StennParanoidPartisan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardType.CREATURE.name(), CardType.LAND.name());
    }

    @Test
    void chosenTypeSpellsCostOneLess() {
        Permanent stenn = addReadyStenn(player1, CardType.SORCERY);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(stenn.getChosenCardType()).isEqualTo(CardType.SORCERY);
    }

    @Test
    void otherCardTypesAreNotReduced() {
        addReadyStenn(player1, CardType.SORCERY);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void activatedAbilityReturnsStennAtNextEndStep() {
        addReadyStenn(player1, CardType.SORCERY);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stenn, Paranoid Partisan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Stenn, Paranoid Partisan"));

        advanceToEndStep();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null) {
            harness.handleListChoice(player1, CardType.SORCERY.name());
        }

        harness.assertOnBattlefield(player1, "Stenn, Paranoid Partisan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Stenn, Paranoid Partisan"));
    }

    @Test
    void returningStennChoosesANewType() {
        addReadyStenn(player1, CardType.SORCERY);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        advanceToEndStep();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardType.INSTANT.name());
        harness.handleListChoice(player1, CardType.INSTANT.name());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof StennParanoidPartisan)
                .findFirst().orElseThrow();
        assertThat(returned.getChosenCardType()).isEqualTo(CardType.INSTANT);
    }

    @Test
    void opponentsSpellsAreNotReduced() {
        addReadyStenn(player1, CardType.SORCERY);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        addReadyStenn(player1, CardType.SORCERY);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void stolenStennReturnsUnderItsOwnersControl() {
        StennParanoidPartisan card = new StennParanoidPartisan();
        card.setOwnerId(player1.getId());
        Permanent stenn = harness.addToBattlefieldAndReturn(player2, card);
        stenn.setChosenCardType(CardType.SORCERY);
        gd.stolenCreatures.put(stenn.getId(), player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Stenn, Paranoid Partisan");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null) {
            harness.handleListChoice(player1, CardType.ARTIFACT.name());
        }

        harness.assertOnBattlefield(player1, "Stenn, Paranoid Partisan");
        harness.assertNotOnBattlefield(player2, "Stenn, Paranoid Partisan");
    }

    @Test
    void activationDuringEndStepWaitsForTheFollowingEndStep() {
        addReadyStenn(player1, CardType.SORCERY);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stenn, Paranoid Partisan");
        assertThat(gd.stack).isEmpty();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null) {
            harness.handleListChoice(player1, CardType.SORCERY.name());
        }

        harness.assertOnBattlefield(player1, "Stenn, Paranoid Partisan");
    }

    private Permanent addReadyStenn(Player player, CardType chosenType) {
        Permanent stenn = harness.addToBattlefieldAndReturn(player, new StennParanoidPartisan());
        stenn.setSummoningSick(false);
        stenn.setChosenCardType(chosenType);
        return stenn;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
