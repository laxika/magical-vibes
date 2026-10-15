package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArchmagesCharm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfRage.class, ArchmagesCharm.class})
class ForceOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two hasty trampling 3/1 Elemental tokens")
    void createsElementalTokens() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getPower()).isEqualTo(3);
            assertThat(elemental.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("Sacrifices the tokens at the caster's next upkeep, not an opponent's")
    void sacrificesTokensAtCastersNextUpkeep() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    @DisplayName("Can exile a red card to cast on an opponent's turn")
    void castsForAlternateCostOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfRage(), new ForceOfRage()));
        harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, 1);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Force of Rage");
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
    }

    @Test
    void cannotUseAlternateCostOnOwnTurn() {
        harness.setHand(player1, List.of(new ForceOfRage(), new ForceOfRage()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, 1)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.exiledCards).isEmpty();
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    void cannotExileNonredCardForAlternateCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ForceOfRage(), new ArchmagesCharm()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, 1)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotExileTheSpellItselfForAlternateCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ForceOfRage()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, 0)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void canPayManaOnOpponentsTurnWithoutExilingACard() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void sacrificesBothTokensWithOneDelayedTrigger() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        advanceToUpkeep(player1);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    void cannotSacrificeATokenControlledByOpponent() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent stolen = findPermanents(player1, "Elemental").getFirst();
        harness.setHand(player2, List.of(new ArchmagesCharm()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, 2, stolen.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentController(gd, stolen.getId())).isEqualTo(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Elemental");
        assertThat(gqs.findPermanentController(gd, stolen.getId())).isEqualTo(player2.getId());
        assertThat(countPermanents(player2, "Elemental")).isEqualTo(1);
    }

    @Test
    void castingDuringUpkeepDoesNotSacrificeNewTokensWithAnEarlierCastsTrigger() {
        harness.setHand(player1, List.of(new ForceOfRage(), new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        List<Permanent> originalTokens = findPermanents(player1, "Elemental");

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2)
                .doesNotContainAnyElementsOf(originalTokens);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Elemental");
    }
}
