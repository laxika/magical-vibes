package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestingPhelddagrif.class, AncientSpider.class, IvoryMask.class})
class QuestingPhelddagrifTest extends BaseCardTest {

    private Permanent addQuestingPhelddagrif(ManaColor mana) {
        Permanent phelddagrif = addCreatureReady(player1, new QuestingPhelddagrif());
        harness.addMana(player1, mana, 1);
        return phelddagrif;
    }

    @Test
    @DisplayName("{G} gets +1/+1 and gives the opponent a 1/1 Hippo token")
    void greenAbilityBoostsAndGivesHippo() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(5);
        assertThat(findPermanents(player2, "Hippo"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HIPPO);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The green ability's boost wears off at end of turn")
    void greenAbilityBoostWearsOff() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(4);
    }

    @Test
    @DisplayName("{W} grants protection from black and red and gives the opponent 2 life")
    void whiteAbilityGrantsProtectionAndLife() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.WHITE);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(phelddagrif.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Protection from the {W} ability wears off at end of turn")
    void protectionWearsOff() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phelddagrif.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("{U} grants flying and offers the opponent a card")
    void blueAbilityGrantsFlyingAndOffersDraw() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.BLUE);
        harness.setLibrary(player2, List.of(new AncientSpider()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The opponent may decline the {U} draw")
    void opponentMayDeclineDraw() {
        addQuestingPhelddagrif(ManaColor.BLUE);
        harness.setLibrary(player2, List.of(new AncientSpider()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Every ability requires an opponent who can legally be targeted")
    void cannotActivateAgainstOpponentWithShroud(int abilityIndex) {
        addQuestingPhelddagrif(manaForAbility(abilityIndex));
        harness.addToBattlefield(player2, new IvoryMask());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("No part of an ability resolves if its opponent gains shroud in response")
    void opponentGainingShroudStopsEntireAbility(int abilityIndex) {
        Permanent phelddagrif = addQuestingPhelddagrif(manaForAbility(abilityIndex));
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new AncientSpider()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, player2.getId());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(4);
        assertThat(phelddagrif.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gqs.hasKeyword(gd, phelddagrif, Keyword.FLYING)).isFalse();
        assertThat(findPermanents(player2, "Hippo")).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying from the blue ability wears off at end of turn")
    void flyingWearsOff() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.BLUE);
        harness.setLibrary(player2, List.of(new AncientSpider()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gqs.hasKeyword(gd, phelddagrif, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, phelddagrif, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated green activations stack and each creates a Hippo")
    void greenActivationsStack() {
        Permanent phelddagrif = addQuestingPhelddagrif(ManaColor.GREEN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phelddagrif)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, phelddagrif)).isEqualTo(6);
        assertThat(findPermanents(player2, "Hippo")).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("The opponent's benefit still resolves after the source leaves the battlefield")
    void opponentBenefitResolvesWithoutSource(int abilityIndex) {
        Permanent phelddagrif = addQuestingPhelddagrif(manaForAbility(abilityIndex));
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new AncientSpider()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(phelddagrif);
        gd.playerGraveyards.get(player1.getId()).add(phelddagrif.getCard());
        harness.passBothPriorities();

        switch (abilityIndex) {
            case 0 -> assertThat(findPermanents(player2, "Hippo")).hasSize(1);
            case 1 -> harness.assertLife(player2, 22);
            case 2 -> {
                assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                        .isEqualTo(player2.getId());
                harness.handleMayAbilityChosen(player2, true);
                assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
            }
            default -> throw new IllegalArgumentException("Unknown ability index: " + abilityIndex);
        }
    }

    private ManaColor manaForAbility(int abilityIndex) {
        return switch (abilityIndex) {
            case 0 -> ManaColor.GREEN;
            case 1 -> ManaColor.WHITE;
            case 2 -> ManaColor.BLUE;
            default -> throw new IllegalArgumentException("Unknown ability index: " + abilityIndex);
        };
    }
}
