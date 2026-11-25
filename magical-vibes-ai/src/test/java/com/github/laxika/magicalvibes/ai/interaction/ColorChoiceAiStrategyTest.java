package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CantPlayChosenNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOptionPreview;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColorChoiceAiStrategyTest {

    private final ColorChoiceAiStrategy strategy = new ColorChoiceAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;
    @Mock
    private Permanent opponentPermanent;
    @Mock
    private Card opponentCard;
    @Mock
    private ActivatedAbility opponentAbility;

    private GameData gameData;
    private UUID aiPlayerId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
    }

    @Test
    @DisplayName("Replacement order applies next the effect that can lead to the least damage")
    void replacementOrderPicksLeastDamage() {
        DamageEvent event = DamageEvent.toPlayer(null, aiPlayerId, 2, false);
        String answer = answerReplacementOrder(event, List.of(
                new ReplacementOptionPreview("Multiply damage to recipient by 2", 3, 6),
                new ReplacementOptionPreview("Prevent 1 damage", 2, 2)));

        assertThat(answer).isEqualTo("Prevent 1 damage");
    }

    @Test
    @DisplayName("Replacement order for a land tapped for mana applies next the effect that can lead to the most mana")
    void replacementOrderPicksMostMana() {
        LandManaEvent event = new LandManaEvent(aiPlayerId, UUID.randomUUID(), Set.of(ManaColor.GREEN), 2,
                new ChoiceContext.ManaColorChoice(aiPlayerId, false, 0), Set.of(), false);
        String answer = answerReplacementOrder(event, List.of(
                new ReplacementOptionPreview("Damping Sphere: produces {C} instead", 1, 2),
                new ReplacementOptionPreview("Mana Reflection: produces 2 times as much", 1, 4)));

        assertThat(answer).isEqualTo("Mana Reflection: produces 2 times as much");
    }

    private String answerReplacementOrder(com.github.laxika.magicalvibes.model.replacement.ReplacementEvent event,
                                          List<ReplacementOptionPreview> previews) {
        List<String> labels = previews.stream().map(ReplacementOptionPreview::label).toList();
        List<ReplacementOption> options = labels.stream()
                .map(label -> new ReplacementOption(label, label, ReplacementOption.Kind.REPLACEMENT, true))
                .toList();
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(aiPlayerId, null, null,
                new ChoiceContext.ReplacementEffectOrder(event, options, previews), labels, "Choose");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        return ((InteractionAnswer.ListChoiceMade) captor.getValue()).choice();
    }

    @Test
    @DisplayName("Competing death replacements pick the plain exile listed first")
    void deathExileReplacementChoicePicksFirstOption() {
        List<ChoiceContext.DeathExileReplacement> options = List.of(
                new ChoiceContext.DeathExileReplacement("Exile it",
                        ChoiceContext.DeathExileReplacement.Kind.PLAIN, null, null, null, null),
                new ChoiceContext.DeathExileReplacement("Kalitas, Traitor of Ghet",
                        ChoiceContext.DeathExileReplacement.Kind.OPPONENT, null, null, null, null));
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(aiPlayerId, null, null,
                new ChoiceContext.DeathExileReplacementChoice(UUID.randomUUID(), options, true),
                List.of("Exile it", "Kalitas, Traitor of Ghet"), "Choose");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("Exile it"));
    }

    @Test
    @DisplayName("Spell creature type choice answers with an offered creature type")
    void answersSpellCreatureTypeChoiceWithOfferedSubtype() throws Exception {
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.SpellCreatureTypeChoice(aiPlayerId),
                List.of("ELF", "WIZARD"),
                "Choose a creature type.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("ELF"));
    }

    @Test
    @DisplayName("Restricted subtype choice answers with an offered creature type")
    void answersRestrictedSubtypeChoiceWithOfferedSubtype() throws Exception {
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.SubtypeChoice(UUID.randomUUID()),
                List.of("ELEMENTAL", "ELF"),
                "Choose a creature type.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("ELEMENTAL"));
    }

    @Test
    @DisplayName("A triggered \"choose up to one\" mode choice (Hullbreaker Horror) answers with an offered mode")
    void answersOptionalTriggeredModalChoiceWithOfferedMode() throws Exception {
        UUID opponentId = UUID.randomUUID();
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerBattlefields.put(opponentId, List.of());
        String permanentMode = "Return target nonland permanent to its owner's hand";
        com.github.laxika.magicalvibes.model.effect.ChooseOneEffect modal =
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(List.of(
                        new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                                permanentMode,
                                com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect.target())),
                        true, 0, 1);

        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.TriggeredModalChoice(null, aiPlayerId, modal, null, false, false, List.of(),
                        null, null, null, false),
                List.of(permanentMode,
                        com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.NO_MODE_LABEL),
                "Choose up to one.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade(permanentMode));
    }

    @Test
    @DisplayName("Free-cast modal choice answers with an offered mode")
    void answersFreeCastModalChoiceWithOfferedMode() throws Exception {
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.LibraryCastModeChoice(null, aiPlayerId,
                        new ChooseOneEffect(List.of()), StackEntryType.SORCERY_SPELL, List.of()),
                List.of("first mode"),
                "Choose one.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("first mode"));
    }

    @Test
    @DisplayName("Exile free-cast modal choice stops after the required mode")
    void answersExileFreeCastModalChoiceWithDoneAfterRequiredMode() throws Exception {
        ChooseOneEffect effect = ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption("first mode", List.of()),
                new ChooseOneEffect.ChooseOneOption("second mode", List.of())));
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.ExileFreeCastModeChoice(null, aiPlayerId, effect,
                        StackEntryType.SORCERY_SPELL, List.of(0), List.of(1), 2, false),
                List.of("second mode", ChooseOneEffect.FINISH_MODE_SELECTION),
                "Choose modes, or Done.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(
                new InteractionAnswer.ListChoiceMade(ChooseOneEffect.FINISH_MODE_SELECTION));
    }

    @Test
    @DisplayName("Card name choice answers with an offered name")
    void answersCardNameChoiceWithOfferedName() throws Exception {
        UUID opponentId = UUID.randomUUID();
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerBattlefields.put(opponentId, List.of(opponentPermanent));
        when(opponentPermanent.getCard()).thenReturn(opponentCard);
        when(opponentCard.getActivatedAbilities(Zone.BATTLEFIELD)).thenReturn(List.of(opponentAbility));
        when(opponentCard.getName()).thenReturn("Plains");

        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.CardNameChoice(null, aiPlayerId, List.of()),
                List.of("Glimmerpost"),
                "Choose a card name.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("Glimmerpost"));
    }

    @Test
    @DisplayName("Card name choice skips an offered name that can't be chosen")
    void cardNameChoiceSkipsUnchoosableOfferedName() {
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(UUID.randomUUID());
        when(gameQueryService.isCardNameUnchoosable(gameData, "Glimpse, the Unthinkable")).thenReturn(true);
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.CardNameChoice(null, aiPlayerId, List.of()),
                List.of("Glimpse, the Unthinkable", "Grizzly Bears"),
                "Choose a card name.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("Grizzly Bears"));
    }

    @Test
    @DisplayName("A name chosen for a reveal from the AI's hand is the one that hand holds most of")
    void nameForOwnHandRevealComesFromThatHand() {
        addOpponent();
        gameData.playerHands.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"), namedCard("Own Twice"),
                namedCard("Own Twice"))));
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(namedCard("In Library"), namedCard("In Library"),
                namedCard("In Library"))));

        assertThat(nameChosenFor(null, RevealCardsFromHandEffect.atRandom(Subjects.you(), 1))).isEqualTo("Own Twice");
    }

    @Test
    @DisplayName("A name chosen for a reveal from the AI's library is the one that library holds most of")
    void nameForOwnLibraryRevealComesFromThatLibrary() {
        addOpponent();
        gameData.playerHands.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"), namedCard("Own Once"),
                namedCard("Own Once"))));
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(namedCard("In Library"), namedCard("Own Twice"),
                namedCard("Own Twice"))));

        assertThat(nameChosenFor(null, new RevealTopCardOfLibraryEffect(Subjects.you()))).isEqualTo("Own Twice");
    }

    @Test
    @DisplayName("Targeting itself, the AI names a card of the hand it reveals")
    void nameForSelfTargetedRevealComesFromOwnHand() {
        addOpponent();
        gameData.playerHands.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"))));

        assertThat(nameChosenFor(aiPlayerId, RevealCardsFromHandEffect.hand(Subjects.targetPlayer())))
                .isEqualTo("Own Once");
    }

    @Test
    @DisplayName("A name chosen for an opponent's hand is a card seen of theirs, never one only hidden zones hold")
    void nameForOpponentHandComesFromPublicZones() {
        UUID opponentId = addOpponent();
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(namedCard("Secret"), namedCard("Secret"))));
        gameData.playerDecks.put(opponentId, new ArrayList<>(List.of(namedCard("Secret"), namedCard("Secret"))));
        Permanent faceDown = new Permanent(namedCard("Secret"));
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gameData.playerBattlefields.put(opponentId, new ArrayList<>(List.of(faceDown)));
        gameData.addToExile(opponentId, namedCard("Secret"), UUID.randomUUID(), true);
        gameData.playerGraveyards.put(opponentId, new ArrayList<>(List.of(namedCard("Seen"))));
        gameData.playerHands.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"))));

        assertThat(nameChosenFor(opponentId, RevealCardsFromHandEffect.hand(Subjects.targetPlayer())))
                .isEqualTo("Seen");
    }

    @Test
    @DisplayName("With nothing of the opponent's seen, the AI names one of its own cards")
    void nameWithNothingSeenComesFromOwnCards() {
        UUID opponentId = addOpponent();
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(namedCard("Secret"))));
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"))));

        assertThat(nameChosenFor(opponentId, RevealCardsFromHandEffect.hand(Subjects.targetPlayer())))
                .isEqualTo("Own Once");
    }

    @Test
    @DisplayName("The AI names only a choosable card among those it knows")
    void nameKnownButUnchoosableIsSkipped() {
        addOpponent();
        when(gameQueryService.isCardNameUnchoosable(eq(gameData), anyString()))
                .thenAnswer(call -> "Own Twice".equals(call.getArgument(1)));
        gameData.playerHands.put(aiPlayerId, new ArrayList<>(List.of(namedCard("Own Once"), namedCard("Own Twice"),
                namedCard("Own Twice"))));

        assertThat(nameChosenFor(null, RevealCardsFromHandEffect.atRandom(Subjects.you(), 1))).isEqualTo("Own Once");
    }

    @Test
    @DisplayName("A dig whose next card's name can't be chosen names another card of the library")
    void digSkipsUnchoosableNameForAnotherLibraryCard() {
        addOpponent();
        when(gameQueryService.isCardNameUnchoosable(eq(gameData), anyString()))
                .thenAnswer(call -> "In Library".equals(call.getArgument(1)));
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(namedCard("In Library"), namedCard("Own Once"))));

        assertThat(nameChosenFor(null, new RevealCardsFromTopUntilEffect(
                new CardHasNameChosenThisResolutionPredicate()))).isEqualTo("Own Once");
    }

    @Test
    @DisplayName("A dig names what the library holds most of, not the card its unknown order puts on top")
    void digDoesNotReadTheLibrarysOrder() {
        addOpponent();
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(namedCard("In Library"), namedCard("Own Twice"),
                namedCard("Own Twice"))));

        assertThat(nameChosenFor(null, new RevealCardsFromTopUntilEffect(
                new CardHasNameChosenThisResolutionPredicate()))).isEqualTo("Own Twice");
    }

    @Test
    @DisplayName("Free-form name guess falls back to another visible card when its pick can't be chosen")
    void freeFormNameGuessAvoidsUnchoosableName() {
        Card glimpse = mock(Card.class);
        Card bears = mock(Card.class);
        when(glimpse.getName()).thenReturn("Glimpse, the Unthinkable");
        when(bears.getName()).thenReturn("Grizzly Bears");
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>(List.of(glimpse, bears)));
        when(gameQueryService.isCardNameUnchoosable(gameData, "Glimpse, the Unthinkable")).thenReturn(true);
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.TargetPlayerNameCardRevealTopChoice(aiPlayerId, aiPlayerId, null, 0),
                List.of(),
                "Choose a card name.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("Grizzly Bears"));
    }

    @Test
    @DisplayName("An as-enters name is taken from what is seen of the opponent, not from a face-down permanent")
    void nameAsEntersComesFromPublicZones() {
        UUID opponentId = addOpponent();
        hideSecretAndShowSeen(opponentId);

        assertThat(nameAnswered(new ChoiceContext.CardNameChoice(null, aiPlayerId, List.of()))).isEqualTo("Seen");
    }

    @Test
    @DisplayName("A name both players choose doesn't come from the opponent's hand")
    void twoPlayerNamingComesFromPublicZones() {
        UUID opponentId = addOpponent();
        hideSecretAndShowSeen(opponentId);

        assertThat(nameAnswered(new ChoiceContext.DualCardNameChoice(namedCard("Source"), aiPlayerId, aiPlayerId, null))).isEqualTo("Seen");
    }

    @Test
    @DisplayName("A name opponents can't cast doesn't come from their hands")
    void nameOpponentsCantCastComesFromPublicZones() {
        UUID opponentId = addOpponent();
        hideSecretAndShowSeen(opponentId);

        assertThat(nameChosenFor(null, CantPlayChosenNameUntilNextTurnEffect.opponentsCantCast())).isEqualTo("Seen");
    }

    @Test
    @DisplayName("A name to exile doesn't come from the searched hand")
    void nameToExileComesFromPublicZones() {
        UUID opponentId = addOpponent();
        hideSecretAndShowSeen(opponentId);

        assertThat(nameChosenFor(opponentId, ExileCardsWithSameNameEffect.upToWithChosenName(4, Subjects.targetPlayer())))
                .isEqualTo("Seen");
    }

    @Test
    @DisplayName("A name for damage and exile doesn't come from the revealed-later hand")
    void nameForDamageAndExileComesFromPublicZones() {
        UUID opponentId = addOpponent();
        hideSecretAndShowSeen(opponentId);

        assertThat(nameChosenFor(opponentId, ExileCardsWithSameNameEffect.allWithChosenName(Subjects.targetPlayer())))
                .isEqualTo("Seen");
    }

    private UUID addOpponent() {
        UUID opponentId = UUID.randomUUID();
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerIds.add(aiPlayerId);
        gameData.playerIds.add(opponentId);
        return opponentId;
    }

    private void hideSecretAndShowSeen(UUID opponentId) {
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(namedCard("Secret"), namedCard("Secret"))));
        gameData.playerDecks.put(opponentId, new ArrayList<>(List.of(namedCard("Secret"), namedCard("Secret"))));
        Card hidden = namedCard("Secret");
        hidden.addActivatedAbility(new ActivatedAbility(true, null, List.of(), "{T}: Nothing."));
        Permanent faceDown = new Permanent(hidden);
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gameData.playerBattlefields.put(opponentId, new ArrayList<>(List.of(faceDown)));
        gameData.playerGraveyards.put(opponentId, new ArrayList<>(List.of(namedCard("Seen"))));
    }

    private String nameAnswered(ChoiceContext context) {
        strategy.answer(new PendingInteraction.ColorChoice(aiPlayerId, null, null, context,
                        List.of("Aardvark", "Secret", "Seen"), "Choose a card name."),
                new AiInteractionContext(gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        return ((InteractionAnswer.ListChoiceMade) captor.getValue()).choice();
    }

    private static Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    /** The name the AI answers with from a catalog-sized list, as a resolution's "choose a card name" step asks. */
    private String nameChosenFor(UUID targetId, CardEffect next) {
        List<String> options = new ArrayList<>();
        for (int i = 0; i < 30_000; i++) {
            options.add("Aardvark %05d".formatted(i));
        }
        options.addAll(List.of("In Library", "Own Once", "Own Twice", "Secret", "Seen"));
        var choose = new ChooseCardNameAtResolutionEffect();
        Card source = namedCard("Source");
        gameData.pendingEffectResolutionEntry = new StackEntry(StackEntryType.SORCERY_SPELL, source, aiPlayerId,
                "Source", List.of(choose, next), targetId, (UUID) null);
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(aiPlayerId, null, null,
                new ChoiceContext.ChooseCardNameAtResolutionChoice(source, aiPlayerId, null, choose, null),
                options, "Choose a card name.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        return ((InteractionAnswer.ListChoiceMade) captor.getValue()).choice();
    }

    @ParameterizedTest(name = "{0} counters on the AI''s own permanent: {1}; on an opponent''s: {2}")
    @CsvSource({"PLUS_ONE_PLUS_ONE, 0, 3", "MINUS_ONE_MINUS_ONE, 3, 0"})
    @DisplayName("A counter removal removes the counters that hurt the AI and keeps those that help it")
    void counterRemovalFollowsTheCountersPolarity(CounterType kind, String fromOwn, String fromOpponent) {
        assertThat(removalAnswer(kind, true)).isEqualTo(fromOwn);
        assertThat(removalAnswer(kind, false)).isEqualTo(fromOpponent);
    }

    /** The AI's answer to removing up to three {@code kind} counters from a permanent it controls or an opponent does. */
    private String removalAnswer(CounterType kind, boolean aiControls) {
        UUID permanentId = UUID.randomUUID();
        when(gameQueryService.findPermanentController(gameData, permanentId))
                .thenReturn(aiControls ? aiPlayerId : UUID.randomUUID());
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(aiPlayerId, null, null,
                new ChoiceContext.MoveCountersAmountChoice(
                        List.of(new ChoiceContext.CounterMoveSlot(permanentId, null, kind)), 0, List.of(), "Rhys"),
                List.of("0", "1", "2", "3"), "Remove how many?");
        org.mockito.Mockito.clearInvocations(gameActions);

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        return ((InteractionAnswer.ListChoiceMade) captor.getValue()).choice();
    }
}
