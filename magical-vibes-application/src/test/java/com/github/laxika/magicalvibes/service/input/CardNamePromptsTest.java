package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithChosenNameEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every "choose a card name" prompt offers what its chooser can know and takes only a card's name
 * (CR 201.4, 400.2, 406.3, 708.4).
 */
@CardUsed({GrizzlyBears.class, Forest.class})
class CardNamePromptsTest extends BaseCardTest {

    private enum Prompt {
        AS_ENTERS(t -> t.beginAsEnters(List.of(), null, false)),
        AS_ENTERS_NONLAND(t -> t.beginAsEnters(List.of(CardType.LAND), null, false)),
        AS_ENTERS_REQUIRED_TYPE(t -> t.beginAsEnters(List.of(), CardType.CREATURE, false)),
        AS_ENTERS_NONBASIC_LAND(t -> t.beginAsEnters(List.of(), null, true)),
        EACH_OF_TWO_PLAYERS(t -> t.harness.inMutationScope(() -> t.harness.getPlayerInputService()
                .beginDualCardNameChoice(t.gd,
                new ChoiceContext.DualCardNameChoice(
                        unlistedCard("Source"), t.player1.getId(), t.player1.getId(), null)))),
        NAMED_AT_RESOLUTION_OF_ANY_CARD(t -> t.resolve(new ChooseCardNameAtResolutionEffect())),
        NAMED_AT_RESOLUTION(t -> t.resolve(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND))),
        LIBRARY_NAME_GUESS(t -> t.harness.inMutationScope(() -> t.harness.getPlayerInputService()
                .beginRevealLibraryNameGuessChoice(
                t.gd, t.player1.getId(), t.player2.getId(), List.of(CardType.LAND), 8, unlistedCard("Source")))),
        SEARCHED_PLAYER_NAMES(t -> t.harness.inMutationScope(() -> t.harness.getPlayerInputService()
                .beginSphinxAmbassadorCardNameChoice(
                t.gd, t.player1.getId(), t.player2.getId()))),
        NAME_FOR_A_GUESS(t -> t.harness.inMutationScope(() -> t.harness.getPlayerInputService()
                .beginLiarsPendulumNameChoice(
                t.gd, t.player1.getId(), t.player2.getId(), null, unlistedCard("Source")))),
        NAMED_TOKEN(t -> t.resolve(new CreateTokenWithChosenNameEffect(
                new CreateTokenEffect("Dog", 1, 1, CardColor.WHITE, List.of(CardSubtype.DOG), Set.of(), Set.of())))),
        TARGET_PLAYER_NAMES(t -> t.resolveTargeting(new TargetPlayerNameCardRevealTopEffect(0), t.player1.getId())),
        EACH_PLAYER_NAMES(t -> t.resolve(new EachPlayerNameCardRevealTopEffect()));

        private final Consumer<CardNamePromptsTest> begin;

        Prompt(Consumer<CardNamePromptsTest> begin) {
            this.begin = begin;
        }
    }

    @ParameterizedTest
    @EnumSource(Prompt.class)
    @DisplayName("A name prompt offers no name of another player's hidden card or of a face-down card")
    void offersNoHiddenName(Prompt prompt) {
        CardType type = prompt == Prompt.AS_ENTERS_NONBASIC_LAND ? CardType.LAND : CardType.CREATURE;
        harness.setHand(player2, List.of(unlistedCard("Hidden Hand Card", type)));
        harness.setLibrary(player2, List.of(unlistedCard("Hidden Library Card", type)));
        Permanent hidden = harness.addToBattlefieldAndReturn(player2, unlistedCard("Hidden Permanent", type));
        hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gd.addToExile(player2.getId(), unlistedCard("Hidden Exiled Card", type), UUID.randomUUID(), true);
        StackEntry hiddenSpell = new StackEntry(StackEntryType.CREATURE_SPELL, unlistedCard("Hidden Spell", type),
                player2.getId(), "Face-down spell", List.of());
        hiddenSpell.setCastFaceDown(true);
        gd.stack.add(hiddenSpell);
        harness.setGraveyard(player2, List.of(unlistedCard("Seen Card", type)));

        prompt.begin.accept(this);

        assertThat(options()).contains("Seen Card").doesNotContain("Hidden Hand Card", "Hidden Library Card",
                "Hidden Permanent", "Hidden Exiled Card", "Hidden Spell");
    }

    @ParameterizedTest
    @EnumSource(value = Prompt.class, mode = EnumSource.Mode.EXCLUDE, names = "NAMED_TOKEN")
    @DisplayName("A name that no card has can't be chosen")
    void takesOnlyACardName(Prompt prompt) {
        prompt.begin.accept(this);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "No Card Has This Name"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("A token's name that no card has can't be chosen as a card name")
    void takesNoTokenName() {
        Card token = unlistedCard("Only A Token's Name");
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        resolve(new ChooseCardNameAtResolutionEffect());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Only A Token's Name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A name limited to the revealed cards can't be another card's name")
    void takesOnlyARevealedName() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.inMutationScope(() -> harness.getPlayerInputService().beginCardNameChoice(gd, player1.getId(),
                unlistedCard("Source"), List.of(CardType.LAND), true));

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Llanowar Elves"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void beginAsEnters(List<CardType> excluded, CardType required, boolean nonbasicLand) {
        harness.inMutationScope(() -> harness.getPlayerInputService().beginCardNameChoice(gd, player1.getId(),
                unlistedCard("Source"), excluded, false, nonbasicLand, null, required));
    }

    private void resolve(CardEffect effect) {
        resolveTargeting(effect, null);
    }

    private void resolveTargeting(CardEffect effect, UUID targetId) {
        gd.stack.add(new StackEntry(StackEntryType.SORCERY_SPELL, unlistedCard("Source", CardType.SORCERY),
                player1.getId(), "Source", List.of(effect), targetId, (UUID) null));
        harness.forceActivePlayer(player1);
        harness.passBothPriorities();
    }

    private List<String> options() {
        return gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options();
    }

    private static Card unlistedCard(String name) {
        return unlistedCard(name, CardType.CREATURE);
    }

    /** A card no catalog lists, so only the cards in the game could offer its name. */
    private static Card unlistedCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }
}
