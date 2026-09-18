package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CabalTherapy;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalTherapy.class, Distress.class, GrizzlyBears.class, SpiritCairn.class})
class SpiritCairnTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W} after an opponent discards creates a flying Spirit")
    void opponentDiscardCreatesSpirit() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player2, new ArrayList<>(List.of(new CabalTherapy())));
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapy");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
                    assertThat(p.getCard().getKeywords()).contains(Keyword.FLYING);
                });
    }

    @Test
    @DisplayName("Paying {W} after the controller discards creates a flying Spirit")
    void controllerDiscardCreatesSpirit() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player1, new ArrayList<>(List.of(new CabalTherapy())));
        harness.setHand(player2, List.of(new CabalTherapy()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Cabal Therapy");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(1)
                .allMatch(p -> p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.WHITE
                        && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && p.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Declining the payment creates no Spirit")
    void decliningPaymentCreatesNoSpirit() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player2, new ArrayList<>(List.of(new CabalTherapy())));
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapy");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    @DisplayName("Each discarded card creates its own may-pay trigger")
    void eachDiscardedCardCreatesSeparateTrigger() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player2, new ArrayList<>(List.of(new CabalTherapy(), new CabalTherapy())));
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapy");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .hasSize(2);
    }

    @Test
    @DisplayName("No Spirit is created when the controller cannot pay {W}")
    void cannotPayCreatesNoSpirit() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player2, new ArrayList<>(List.of(new CabalTherapy())));
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapy");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    @DisplayName("Creates one Spirit for each card a player discards")
    void createsOneSpiritPerDiscardedCard() {
        harness.addToBattlefield(player1, new SpiritCairn());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new Distress(), new Distress())));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        for (int i = 0; i < 2; i++) {
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            while (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .hasSize(2);
    }
}
