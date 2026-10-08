package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsLaboratory.class})
class VolrathsLaboratoryTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color and creature type stores both choices")
    void choosingColorAndSubtypeStoresChoices() {
        harness.setHand(player1, List.of(new VolrathsLaboratory()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GOBLIN");

        Permanent laboratory = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(laboratory.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(laboratory.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The activated ability creates a token with both chosen characteristics")
    void activatedAbilityCreatesChosenToken() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new VolrathsLaboratory());
        laboratory.setChosenColor(CardColor.RED);
        laboratory.setChosenSubtype(CardSubtype.GOBLIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(token.getCard().getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability pays five generic mana and taps the laboratory")
    void activatedAbilityPaysManaAndTapsLaboratory() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new VolrathsLaboratory());
        laboratory.setChosenColor(CardColor.RED);
        laboratory.setChosenSubtype(CardSubtype.GOBLIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("A freshly cast laboratory can immediately create the token chosen as it entered")
    void freshlyCastLaboratoryCreatesChosenToken() {
        harness.setHand(player1, List.of(new VolrathsLaboratory()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "MERFOLK");

        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MERFOLK);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Two laboratories retain independent entry choices")
    void laboratoriesRetainIndependentChoices() {
        harness.setHand(player1, List.of(new VolrathsLaboratory(), new VolrathsLaboratory()));
        harness.addMana(player1, ManaColor.COLORLESS, 20);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GOBLIN");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "ELF");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent firstToken = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(firstToken.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(firstToken.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent secondToken = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(secondToken.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(secondToken.getCard().getSubtypes()).containsExactly(CardSubtype.ELF);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Insufficient mana does not tap the laboratory or create an ability")
    void insufficientManaDoesNotPayActivationCosts() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new VolrathsLaboratory());
        laboratory.setChosenColor(CardColor.RED);
        laboratory.setChosenSubtype(CardSubtype.GOBLIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(laboratory.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(laboratory);
    }
}
