package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaffWeatherlightStalwart.class, GrizzlyBears.class, LightningBolt.class})
class RaffWeatherlightStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant offers the tap-two-creatures draw choice")
    void castingInstantOffersTapAndDrawChoice() {
        addRaffAndTwoCreatures();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Tapping two creatures after accepting draws a card")
    void acceptingTapCostDrawsCard() {
        addRaffAndTwoCreatures();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        tapTwoCreatures(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .filter(Permanent::isTapped)
                .count()).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the tap choice does not draw or tap creatures")
    void decliningTapChoiceDoesNothing() {
        addRaffAndTwoCreatures();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .filter(Permanent::isTapped)
                .count()).isZero();
    }

    @Test
    @DisplayName("The activated ability boosts creatures and grants vigilance until end of turn")
    void activatedAbilityBoostsAndGrantsVigilance() {
        Permanent raff = addCreatureReady(player1, new RaffWeatherlightStalwart());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raff.getEffectivePower()).isEqualTo(2);
        assertThat(raff.getEffectiveToughness()).isEqualTo(4);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(raff.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raff.getEffectivePower()).isEqualTo(1);
        assertThat(raff.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(raff.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(bear.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    private void addRaffAndTwoCreatures() {
        addCreatureReady(player1, new RaffWeatherlightStalwart());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
    }

    private void tapTwoCreatures(com.github.laxika.magicalvibes.model.Player player) {
        List<Permanent> creatures = gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .filter(permanent -> !permanent.getCard().getName().equals("Raff, Weatherlight Stalwart"))
                .filter(permanent -> !permanent.isTapped())
                .limit(2)
                .toList();
        creatures.forEach(creature -> harness.handlePermanentChosen(player, creature.getId()));
    }
}
