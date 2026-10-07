package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

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
        harness.handleCardChosen(player1, 0);

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

    @Test
    void tokenChoiceIsMadeOnResolutionAndFontRemainsTapped() {
        Permanent font = addFont();

        harness.activateAbility(player1, battlefieldIndex(font), 0, null, null);

        assertThat(font.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Food token");

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(font.isTapped()).isTrue();
    }

    @Test
    void foodCanBeSacrificedForThreeLife() {
        Permanent font = addFont();
        createToken(font, "Create a Food token");
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(food), 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void clueCanBeSacrificedToDrawWithoutTapping() {
        Permanent font = addFont();
        createToken(font, "Create a Clue token");
        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TransmutationFont()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(clue), 0, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Transmutation Font");
    }

    @Test
    void bloodRequiresDiscardAndSacrificeBeforeDrawing() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        Permanent blood = findPermanent(player1, "Blood");
        Card discarded = new TransmutationFont();
        Card drawn = new TransmutationFont();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(blood), 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Blood");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "BEGINNING_OF_COMBAT", "END_STEP"})
    void tutorCannotBeActivatedOutsideMainPhase(TurnStep step) {
        Permanent font = prepareTutor();
        harness.forceStep(step);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");

        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void tutorCannotBeActivatedOnOpponentsTurn() {
        Permanent font = prepareTutor();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void tutorCannotBeActivatedWithNonemptyStack() {
        Permanent font = prepareTutor();
        Permanent otherFont = harness.addToBattlefieldAndReturn(player1, new TransmutationFont());
        harness.activateAbility(player1, battlefieldIndex(otherFont), 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nontokenArtifactCannotCompleteSacrificeCost() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Clue token");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void tutorMayFailToFindEvenWhenArtifactIsAvailable() {
        Permanent font = prepareTutor();
        Card artifact = new TransmutationFont();
        harness.setLibrary(player1, List.of(artifact));

        harness.activateAbility(player1, battlefieldIndex(font), 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Transmutation Font")).containsExactly(font);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(font.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tutorWithEmptyLibraryStillPaysAllCosts() {
        Permanent font = prepareTutor();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, battlefieldIndex(font), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(font);
        assertThat(font.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedTokensCanPaySacrificeCost() {
        Permanent font = prepareTutor();
        findPermanent(player1, "Blood").tap();
        findPermanent(player1, "Clue").tap();
        findPermanent(player1, "Food").tap();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, battlefieldIndex(font), 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(font);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsTokensCannotPaySacrificeCost() {
        Permanent font = prepareTutor();
        Permanent food = findPermanent(player1, "Food");
        gd.playerBattlefields.get(player1.getId()).remove(food);
        gd.playerBattlefields.get(player2.getId()).add(food);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(font), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(font.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Food")).containsExactly(food);
    }

    @Test
    void tokenCreationCanBeActivatedOnOpponentsTurn() {
        Permanent font = addFont();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, battlefieldIndex(font), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Clue token");

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(font.isTapped()).isTrue();
    }

    private Permanent prepareTutor() {
        Permanent font = addFont();
        createToken(font, "Create a Blood token");
        createToken(font, "Create a Clue token");
        createToken(font, "Create a Food token");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return font;
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
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        token.setSummoningSick(false);
        return token;
    }
}
