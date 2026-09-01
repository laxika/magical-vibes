package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TransmutationFont.class, GoldMyr.class})
class TransmutationFontTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({
            "Create a Blood token, Blood",
            "Create a Clue token, Clue",
            "Create a Food token, Food"
    })
    @DisplayName("The first ability creates the chosen token")
    void createsChosenToken(String choice, String tokenName) {
        Permanent font = addFont();

        createToken(font, choice);

        assertThat(findPermanents(player1, tokenName)).hasSize(1);
        assertThat(font.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The tutor ability sacrifices three artifact tokens with different names")
    void sacrificesDistinctNamesAndSearchesForArtifact() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Clue token");
        createToken(font, "Create a Food token");
        harness.setLibrary(player1, List.of(new GoldMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(font), 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> List.of("Blood", "Clue", "Food").contains(permanent.getCard().getName()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("The tutor ability cannot be activated with only two distinct token names")
    void requiresDistinctNames() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Clue token");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Duplicate token names are excluded from later payment choices")
    void excludesDuplicateNamesFromPaymentChoices() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Clue token");
        createToken(font, "Create a Food token");
        addArtifactToken("Treasure");
        List<Permanent> bloodTokens = findPermanents(player1, "Blood");
        Permanent clue = findPermanent(player1, "Clue");
        Permanent food = findPermanent(player1, "Food");
        Permanent treasure = findPermanent(player1, "Treasure");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(font), 1, null, null);
        harness.handlePermanentChosen(player1, bloodTokens.get(0).getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(clue.getId(), food.getId(), treasure.getId());

        harness.handlePermanentChosen(player1, clue.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(food.getId(), treasure.getId());
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clue, food, bloodTokens.get(0));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bloodTokens.get(1), treasure, font);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addFont() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new TransmutationFont());
        font.setSummoningSick(false);
        return font;
    }

    private void createToken(Permanent font, String choice) {
        harness.activateAbility(player1, battlefieldIndex(font), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, choice);
        font.untap();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addArtifactToken(String name) {
        Card tokenCard = new Card();
        tokenCard.setName(name);
        tokenCard.setType(CardType.ARTIFACT);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(null);
        Permanent token = new Permanent(tokenCard);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(token);
        return token;
    }
}
