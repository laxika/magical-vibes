package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SoulOfNewPhyrexia;
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

@CardUsed({PhyrexianScrapyard.class, SoulOfNewPhyrexia.class})
class PhyrexianScrapyardTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent scrapyard = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(scrapyard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discarding a card conjures Phyrexian Scrapyard into hand")
    void discardingConjuresScrapyard() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.setHand(player1, List.of(new PhyrexianScrapyard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phyrexian Scrapyard");
        harness.assertInHand(player1, "Phyrexian Scrapyard");
    }

    @Test
    @DisplayName("Sacrificing three Scrapyards conjures Soul of New Phyrexia")
    void sacrificingThreeScrapyardsConjuresSoul() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Phyrexian Scrapyard"))
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SoulOfNewPhyrexia);
    }

    @Test
    @DisplayName("The Soul of New Phyrexia ability is sorcery speed")
    void soulAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard is paid before the conjured card arrives")
    void discardIsAnActivationCost() {
        Permanent scrapyard = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        PhyrexianScrapyard discarded = new PhyrexianScrapyard();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(scrapyard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(card -> {
            assertThat(card).isInstanceOf(PhyrexianScrapyard.class);
            assertThat(card.getId()).isNotEqualTo(discarded.getId());
            assertThat(card.getOwnerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Discard ability can be activated during the opponent's upkeep")
    void discardAbilityWorksOnOpponentsTurn() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.setHand(player1, List.of(new PhyrexianScrapyard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phyrexian Scrapyard");
        harness.assertNotInHand(player2, "Phyrexian Scrapyard");
    }

    @Test
    @DisplayName("Discard ability cannot be activated with an empty hand")
    void discardAbilityRequiresCardInHand() {
        Permanent scrapyard = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scrapyard.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's Scrapyards cannot pay the sacrifice cost")
    void sacrificeCostRequiresThreeControlledScrapyards() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player2, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Soul ability cannot be activated during the opponent's main phase")
    void soulAbilityRequiresControllersTurn() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activating Scrapyard need not be one of the three sacrificed lands")
    void canSacrificeOtherScrapyards() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).hasSize(2);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Soul of New Phyrexia");
    }

    @Test
    @DisplayName("Soul ability cannot be activated while another ability is on the stack")
    void soulAbilityRequiresEmptyStack() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.setHand(player1, List.of(new PhyrexianScrapyard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        harness.assertNotOnBattlefield(player1, "Soul of New Phyrexia");
    }

    @Test
    @DisplayName("Three lands are sacrificed before Soul is conjured")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addToBattlefield(player1, new PhyrexianScrapyard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isInstanceOf(SoulOfNewPhyrexia.class);
            assertThat(permanent.getCard().getOwnerId()).isEqualTo(player1.getId());
            assertThat(permanent.isTapped()).isFalse();
        });
        harness.assertNotOnBattlefield(player2, "Soul of New Phyrexia");
    }
}
