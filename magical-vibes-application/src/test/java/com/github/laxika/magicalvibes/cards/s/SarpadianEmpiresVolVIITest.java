package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarpadianEmpiresVolVII.class, AncientGrudge.class})
class SarpadianEmpiresVolVIITest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color stores the selected option")
    void choosingColorStoresSelectedOption() {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(artifact.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("The activated ability creates the token paired with the chosen color")
    void activatedAbilityCreatesPairedToken() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SarpadianEmpiresVolVII());
        artifact.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
    }

    @ParameterizedTest
    @CsvSource({"WHITE,CITIZEN", "BLUE,CAMARID", "BLACK,THRULL", "RED,GOBLIN", "GREEN,SAPROLING"})
    @DisplayName("Each entrance choice creates exactly one matching 1/1 creature")
    void everyChoiceCreatesMatchingToken(CardColor color, CardSubtype subtype) {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .singleElement().satisfies(token -> {
                    assertThat(gqs.isCreature(gd, token)).isTrue();
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(color);
                    assertThat(token.getCard().getSubtypes()).containsExactly(subtype);
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An activation still creates the chosen token after the artifact is destroyed")
    void createsTokenAfterSourceIsDestroyed() {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAMARID);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Separate copies retain independent choices")
    void copiesRetainIndependentChoices() {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII(), new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(tokens.get(0).getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(tokens.get(1).getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.get(1).getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Activating again after untapping retains the original choice")
    void repeatedActivationsRetainChoice() {
        harness.setHand(player1, List.of(new SarpadianEmpiresVolVII()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        artifact.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2).allSatisfy(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THRULL);
                });
    }

    @Test
    @DisplayName("The activated ability cannot be used with only two mana")
    void activationRequiresThreeMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SarpadianEmpiresVolVII());
        artifact.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(artifact);
    }
}
