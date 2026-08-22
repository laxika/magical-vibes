package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Canonical projection of every promptable pending interaction to its existing wire message.
 *
 * <p>Strategies are exact-class keyed. There is deliberately no generic fallback: adding a
 * promptable {@link PendingInteraction} requires an explicit projection strategy, and duplicate
 * registrations fail construction. The authoritative pending interaction remains on
 * {@link GameData}; callers must verify its stable decision identity before projecting it.
 */
@Component
public class InteractionPromptProjectionRegistry {

    private final CardViewFactory cardViewFactory;
    private final Map<Class<? extends PendingInteraction>, ProjectionStrategy<?>> strategies =
            new LinkedHashMap<>();

    public InteractionPromptProjectionRegistry(CardViewFactory cardViewFactory) {
        this.cardViewFactory = cardViewFactory;

        register(PendingInteraction.XValueChoice.class, this::projectXValueChoice);
        register(PendingInteraction.AlternateCastXValueChoice.class, this::projectAlternateCastXValueChoice);
        register(PendingInteraction.Scry.class, this::projectScry);
        register(PendingInteraction.HandTopBottomChoice.class, this::projectHandTopBottomChoice);
        register(PendingInteraction.HandBottomExileChoice.class, this::projectHandBottomExileChoice);
        register(PendingInteraction.LibraryReorder.class, this::projectLibraryReorder);
        register(PendingInteraction.MayAbilityChoice.class, this::projectMayAbilityChoice);
        register(PendingInteraction.KnowledgePoolCastChoice.class, this::projectKnowledgePoolCastChoice);
        register(PendingInteraction.ImprovisationCapstoneCastChoice.class,
                this::projectImprovisationCapstoneCastChoice);
        register(PendingInteraction.ExiledSpellCopyChoice.class, this::projectExiledSpellCopyChoice);
        register(PendingInteraction.AssimilationAegisCopyChoice.class,
                this::projectAssimilationAegisCopyChoice);
        register(PendingInteraction.TargetHandSpellCopyChoice.class,
                this::projectTargetHandSpellCopyChoice);
        register(PendingInteraction.ExiledCardMayPlayChoice.class, this::projectExiledCardMayPlayChoice);
        register(PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class,
                this::projectExileInstantOrSorcerySpellCostChoice);
        register(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class,
                this::projectPutCardExiledWithSourceIntoGraveyardCostChoice);
        register(PendingInteraction.BrilliantUltimatumPileSeparationChoice.class,
                this::projectBrilliantUltimatumPileSeparationChoice);
        register(PendingInteraction.BrilliantUltimatumPileChoice.class,
                this::projectBrilliantUltimatumPileChoice);
        register(PendingInteraction.BrilliantUltimatumPlayChoice.class,
                this::projectBrilliantUltimatumPlayChoice);
        register(PendingInteraction.HostileNegotiationsFaceUpChoice.class,
                this::projectHostileNegotiationsFaceUpChoice);
        register(PendingInteraction.HostileNegotiationsOpponentPileChoice.class,
                this::projectHostileNegotiationsOpponentPileChoice);
        register(PendingInteraction.MirrorOfFateChoice.class, this::projectMirrorOfFateChoice);
        register(PendingInteraction.KeepCardsInHandChoice.class, this::projectKeepCardsInHandChoice);
        register(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class,
                this::projectEachPlayerChoosesOneCardOfEachColorChoice);
        register(PendingInteraction.PutLandsFromHandChoice.class, this::projectPutLandsFromHandChoice);
        register(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class,
                this::projectEachPlayerMayPutCardFromHandChoice);
        register(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class,
                this::projectRevealAnyNumberOfCardsFromHandChoice);
        register(PendingInteraction.DoomsdayChoice.class, this::projectDoomsdayChoice);
        register(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class,
                this::projectSearchLibraryAndOrGraveyardChoice);
        register(PendingInteraction.SearchLibraryToTopChoice.class,
                this::projectSearchLibraryToTopChoice);
        register(PendingInteraction.IntuitionSearchChoice.class, this::projectIntuitionSearchChoice);
        register(PendingInteraction.EcologicalAppreciationSearchChoice.class,
                this::projectEcologicalAppreciationSearchChoice);
        register(PendingInteraction.EcologicalAppreciationOpponentChoice.class,
                this::projectEcologicalAppreciationOpponentChoice);
        register(PendingInteraction.VerdantMasterySearchChoice.class,
                this::projectVerdantMasterySearchChoice);
        register(PendingInteraction.VerdantMasteryLandChoice.class,
                this::projectVerdantMasteryLandChoice);
        register(PendingInteraction.GuidedPassageChoice.class, this::projectGuidedPassageChoice);
        register(PendingInteraction.PermanentAuctionChoice.class, this::projectPermanentAuctionChoice);
        register(PendingInteraction.IllicitAuctionBidChoice.class, this::projectIllicitAuctionBidChoice);
        register(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class,
                this::projectExileNonlandCardFromTargetHandOrGraveyardChoice);
        register(PendingInteraction.MagesContestBidChoice.class, this::projectMagesContestBidChoice);
        register(PendingInteraction.PainsRewardBidChoice.class, this::projectPainsRewardBidChoice);
        register(PendingInteraction.MultiZoneExileChoice.class, this::projectMultiZoneExileChoice);
        register(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class,
                this::projectPutUpToCardsFromHandOntoBattlefieldChoice);
        register(PendingInteraction.PutCardFromHandOrGraveyardChoice.class,
                this::projectPutCardFromHandOrGraveyardChoice);
        register(PendingInteraction.ExilePermanentsOrHandCardsChoice.class,
                this::projectExilePermanentsOrHandCardsChoice);
        register(PendingInteraction.BeholdChoice.class, this::projectBeholdChoice);
        register(PendingInteraction.AttachAurasChoice.class, this::projectAttachAurasChoice);
        register(PendingInteraction.MultiPermanentChoice.class, this::projectMultiPermanentChoice);
        register(PendingInteraction.MultiGraveyardChoice.class, this::projectMultiGraveyardChoice);
        register(PendingInteraction.ColorChoice.class, this::projectColorChoice);
        register(PendingInteraction.RevealedHandChoice.class, this::projectRevealedHandChoice);
        register(PendingInteraction.RevealCardsDiscardChoice.class,
                this::projectRevealCardsDiscardChoice);
        register(PendingInteraction.AlternatingHandExileChoice.class,
                this::projectAlternatingHandExileChoice);
        register(PendingInteraction.GraveyardChoice.class, this::projectGraveyardChoice);
        register(PendingInteraction.GraveyardExileCostChoice.class,
                this::projectGraveyardExileCostChoice);
        register(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class,
                this::projectActivatedAbilityGraveyardExileCostChoice);
        register(PendingInteraction.HandCardChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, true));
        register(PendingInteraction.StrongholdGambitCardChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.MasterOfPredicamentsCardChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.TargetedHandCardChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, true));
        register(PendingInteraction.DiscardChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction,
                        ((PendingInteraction.DiscardChoice) interaction).declinable()));
        register(PendingInteraction.ExileFromHandChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.ImprintFromHandChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.ExileFromHandWithRefineCountersChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.DiscardCostChoice.class,
                (gameData, interaction) -> projectHandChoice(interaction, false));
        register(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                this::projectPutCardsFromHandOnLibraryCardChoice);
        register(PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.class,
                this::projectPutCardsFromHandOnLibraryDestinationChoice);
        register(PendingInteraction.TargetLibraryDestinationChoice.class,
                this::projectTargetLibraryDestinationChoice);
        register(PendingInteraction.CounteredSpellLibraryDestinationChoice.class,
                this::projectCounteredSpellLibraryDestinationChoice);
        register(PendingInteraction.SylvanLibraryChoice.class, this::projectSylvanLibraryChoice);
        register(PendingInteraction.LibraryRevealChoice.class, this::projectLibraryRevealChoice);
        register(PendingInteraction.VividCardChoice.class, this::projectVividCardChoice);
        register(PendingInteraction.NivMizzetColorPairChoice.class, this::projectNivMizzetColorPairChoice);
        register(PendingInteraction.LibrarySearch.class, this::projectLibrarySearch);
        register(PendingInteraction.SearchOutsideGameOrExileCardChoice.class,
                this::projectSearchOutsideGameOrExileCardChoice);
        register(PendingInteraction.FaceUpExiledCardChoice.class,
                this::projectFaceUpExiledCardChoice);
        register(PendingInteraction.PermanentChoice.class, this::projectPermanentChoice);
        register(PendingInteraction.AdNauseamRepeatChoice.class, this::projectAdNauseamRepeatChoice);
        register(PendingInteraction.ForbiddenRitualRepeatChoice.class, this::projectForbiddenRitualRepeatChoice);
        register(PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice.class,
                this::projectExiledPermanentPutOntoBattlefieldChoice);
        register(PendingInteraction.LimDulsVaultRepeatChoice.class, this::projectLimDulsVaultRepeatChoice);
        register(PendingInteraction.LimDulsVaultOrderChoice.class, this::projectLimDulsVaultOrderChoice);
        register(PendingInteraction.AttackerDeclaration.class, this::projectAttackerDeclaration);
        register(PendingInteraction.BlockerDeclaration.class, this::projectBlockerDeclaration);
        register(PendingInteraction.CombatDamageAssignment.class,
                this::projectCombatDamageAssignment);
    }

    /**
     * Builds the prompt for one exact interaction kind. An empty result is the explicit
     * no-wire strategy used by Karn Scion library-reveal choices whose prompt is null.
     */
    public Optional<Object> project(
            GameData gameData, PendingInteraction interaction) {
        ProjectionStrategy<PendingInteraction> strategy = strategyFor(interaction);
        return Optional.ofNullable(strategy.project(gameData, interaction));
    }

    /** Projects the mulligan bottoming decision used by initial delivery and reconnect replay. */
    public SelectCardsToBottomMessage projectCardsToBottom(int count) {
        return new SelectCardsToBottomMessage(count);
    }

    /** Exact interaction classes with one registered prompt strategy. */
    public Set<Class<? extends PendingInteraction>> registeredTypes() {
        return Set.copyOf(strategies.keySet());
    }

    private InteractionPromptMessage projectXValueChoice(
            GameData gameData, PendingInteraction.XValueChoice interaction) {
        return InteractionPromptMessage.numberPick(
                interaction.prompt(), interaction.minValue(), interaction.maxValue(),
                interaction.cardName(), interaction.manaPayment());
    }

    private InteractionPromptMessage projectAlternateCastXValueChoice(
            GameData gameData, PendingInteraction.AlternateCastXValueChoice interaction) {
        return InteractionPromptMessage.numberPick(
                interaction.prompt(), 0, interaction.maxValue(), interaction.cardName(), true);
    }

    private InteractionPromptMessage projectScry(
            GameData gameData, PendingInteraction.Scry interaction) {
        int count = interaction.cards().size();
        String prompt;
        if (interaction.toGraveyard()) {
            prompt = count == 1
                    ? "Surveil 1: Keep on top of your library or put into your graveyard."
                    : "Surveil " + count
                            + ": Put cards on top of your library or into your graveyard.";
        } else {
            boolean ownLibrary = interaction.playerId().equals(interaction.libraryOwnerId());
            String library = ownLibrary
                    ? "your library"
                    : gameData.playerIdToName.get(interaction.libraryOwnerId()) + "'s library";
            prompt = ownLibrary
                    ? count == 1
                            ? "Scry 1: Keep on top or put on the bottom of your library."
                            : "Scry " + count + ": Put cards on the top or bottom of your library."
                    : count == 1
                            ? "Keep on top or put on the bottom of " + library + "."
                            : "Put cards on the top or bottom of " + library + ".";
        }
        return InteractionPromptMessage.scryOrder(
                cardViews(interaction.cards()), prompt, interaction.toGraveyard());
    }

    private InteractionPromptMessage projectHandTopBottomChoice(
            GameData gameData, PendingInteraction.HandTopBottomChoice interaction) {
        return InteractionPromptMessage.handTopBottom(
                cardViews(interaction.cards()),
                "Look at the top " + interaction.cards().size()
                        + " cards of your library. Choose one to put into your hand.");
    }

    private InteractionPromptMessage projectHandBottomExileChoice(
            GameData gameData, PendingInteraction.HandBottomExileChoice interaction) {
        return InteractionPromptMessage.handBottomExile(
                cardViews(interaction.cards()),
                "Look at the top " + interaction.cards().size()
                        + " cards of your library. Choose one to put into your hand and one to put"
                        + " on the bottom of your library. Exile the rest.");
    }

    private InteractionPromptMessage projectLibraryReorder(
            GameData gameData, PendingInteraction.LibraryReorder interaction) {
        return InteractionPromptMessage.cardOrder(
                cardViews(interaction.cards()), interaction.prompt());
    }

    private InteractionPromptMessage projectMayAbilityChoice(
            GameData gameData, PendingInteraction.MayAbilityChoice interaction) {
        boolean canPay = true;
        if (interaction.manaCost() != null) {
            ManaCost cost = new ManaCost(interaction.manaCost());
            ManaPool pool = gameData.playerManaPools.get(interaction.playerId());
            canPay = cost.hasX() ? cost.calculateMaxX(pool) > 0 : cost.canPay(pool);
        }
        return InteractionPromptMessage.acceptDecline(
                interaction.description(), canPay, interaction.manaCost());
    }

    private InteractionPromptMessage projectKnowledgePoolCastChoice(
            GameData gameData, PendingInteraction.KnowledgePoolCastChoice interaction) {
        List<CardView> cardViews = new ArrayList<>();
        PendingKnowledgePoolCast pendingCast =
                gameData.peekPendingInteraction(PendingKnowledgePoolCast.class);
        UUID sourcePermanentId = pendingCast != null ? pendingCast.sourcePermanentId() : null;
        if (sourcePermanentId != null) {
            for (Card card : gameData.getCardsExiledByPermanent(sourcePermanentId)) {
                if (interaction.validCardIds().contains(card.getId())) {
                    cardViews.add(cardViewFactory.create(card));
                }
            }
        }
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Knowledge Pool — you may cast a nonland card without paying its mana cost.");
    }

    private InteractionPromptMessage projectImprovisationCapstoneCastChoice(
            GameData gameData, PendingInteraction.ImprovisationCapstoneCastChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                exiledCardViews(gameData, interaction.validCardIds()),
                interaction.maxCount(),
                "You may cast any number of spells from among the exiled cards without paying "
                        + "their mana costs.");
    }

    private InteractionPromptMessage projectExiledSpellCopyChoice(
            GameData gameData, PendingInteraction.ExiledSpellCopyChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                exiledCardViews(gameData, interaction.validCardIds()),
                1,
                "Choose " + interaction.choiceDescription() + " exiled this way to copy "
                        + interaction.copies() + " times.");
    }

    private InteractionPromptMessage projectTargetHandSpellCopyChoice(
            GameData gameData, PendingInteraction.TargetHandSpellCopyChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.cards()),
                1,
                "You may choose an instant or sorcery card to copy.");
    }

    private InteractionPromptMessage projectExiledCardMayPlayChoice(
            GameData gameData, PendingInteraction.ExiledCardMayPlayChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                exiledCardViews(gameData, interaction.validCardIds()),
                1,
                "Choose a card exiled this way to play until the end of your next turn.");
    }

    private InteractionPromptMessage projectExileInstantOrSorcerySpellCostChoice(
            GameData gameData, PendingInteraction.ExileInstantOrSorcerySpellCostChoice interaction) {
        List<CardView> cards = gameData.stack.stream()
                .filter(entry -> interaction.validCardIds().contains(entry.getCard().getId()))
                .map(StackEntry::getCard)
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(), cards, 1,
                "Choose an instant or sorcery spell you control to exile as an activation cost.");
    }

    private InteractionPromptMessage projectPutCardExiledWithSourceIntoGraveyardCostChoice(
            GameData gameData,
            PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(),
                exiledCardViews(gameData, interaction.validCardIds()),
                1,
                "Choose a card exiled with this permanent to put into its owner's graveyard as an activation cost.");
    }

    private InteractionPromptMessage projectBrilliantUltimatumPileSeparationChoice(
            GameData gameData,
            PendingInteraction.BrilliantUltimatumPileSeparationChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(),
                exiledCardViews(gameData, interaction.validCardIds()),
                interaction.validCardIds().size(),
                "Separate the exiled cards into two piles. Select cards for Pile 1 "
                        + "(unselected form Pile 2).");
    }

    private InteractionPromptMessage projectBrilliantUltimatumPileChoice(
            GameData gameData, PendingInteraction.BrilliantUltimatumPileChoice interaction) {
        String pile1 = describeExiledPile(gameData, interaction.pile1CardIds());
        String pile2 = describeExiledPile(gameData, interaction.pile2CardIds());
        return InteractionPromptMessage.acceptDecline(
                "Choose a pile to play lands and cast spells from. Yes = Pile 1 ("
                        + pile1 + "), No = Pile 2 (" + pile2 + ").",
                true,
                null);
    }

    private InteractionPromptMessage projectBrilliantUltimatumPlayChoice(
            GameData gameData, PendingInteraction.BrilliantUltimatumPlayChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                exiledCardViews(gameData, interaction.validCardIds()),
                interaction.maxCount(),
                "You may play lands and cast spells from this pile without paying their mana costs.");
    }

    private InteractionPromptMessage projectHostileNegotiationsFaceUpChoice(
            GameData gameData, PendingInteraction.HostileNegotiationsFaceUpChoice interaction) {
        return InteractionPromptMessage.acceptDecline(
                "Choose a pile to turn face up. Yes = Pile 1 ("
                        + describeCards(interaction.pile1Cards()) + "), No = Pile 2 ("
                        + describeCards(interaction.pile2Cards()) + ").",
                true, null);
    }

    private InteractionPromptMessage projectHostileNegotiationsOpponentPileChoice(
            GameData gameData, PendingInteraction.HostileNegotiationsOpponentPileChoice interaction) {
        String pile1 = interaction.pile1FaceUp()
                ? describeCards(interaction.pile1Cards())
                : "face down, " + interaction.pile1Cards().size() + " cards";
        String pile2 = interaction.pile1FaceUp()
                ? "face down, " + interaction.pile2Cards().size() + " cards"
                : describeCards(interaction.pile2Cards());
        return InteractionPromptMessage.acceptDecline(
                "Choose a pile to put into the controller's hand. Yes = Pile 1 ("
                        + pile1 + "), No = Pile 2 (" + pile2 + ").",
                true, null);
    }

    private InteractionPromptMessage projectMirrorOfFateChoice(
            GameData gameData, PendingInteraction.MirrorOfFateChoice interaction) {
        List<CardView> cardViews = gameData.getPlayerExiledCards(interaction.playerId()).stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose up to seven face-up exiled cards you own to put on top of your library.");
    }

    private InteractionPromptMessage projectKeepCardsInHandChoice(
            GameData gameData, PendingInteraction.KeepCardsInHandChoice interaction) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of() : hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose up to seven cards in your hand to keep. Shuffle the rest into your library.");
    }

    private InteractionPromptMessage projectEachPlayerChoosesOneCardOfEachColorChoice(
            GameData gameData, PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice interaction) {
        return InteractionPromptMessage.cardIndexPick(
                interaction.validIndices(),
                "Choose a " + colorName(interaction.colorIndex()) + " card to keep for "
                        + interaction.sourceName() + ".", false);
    }

    private String colorName(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> "white";
            case 1 -> "blue";
            case 2 -> "black";
            case 3 -> "red";
            case 4 -> "green";
            default -> throw new IllegalArgumentException("Unknown card color index: " + colorIndex);
        };
    }

    private InteractionPromptMessage projectPutLandsFromHandChoice(
            GameData gameData, PendingInteraction.PutLandsFromHandChoice interaction) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of() : hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.validCardIds().size(),
                "You may put any number of land cards from your hand onto the battlefield.");
    }

    private InteractionPromptMessage projectEachPlayerMayPutCardFromHandChoice(
            GameData gameData, PendingInteraction.EachPlayerMayPutCardFromHandChoice interaction) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of() : hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1,
                "You may put an " + interaction.label() + " card from your hand onto the battlefield.");
    }

    private InteractionPromptMessage projectRevealAnyNumberOfCardsFromHandChoice(
            GameData gameData, PendingInteraction.RevealAnyNumberOfCardsFromHandChoice interaction) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of() : hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews,
                interaction.activatedAbilityContext() == null
                        ? interaction.validCardIds().size()
                        : interaction.activatedAbilityContext().xValue(),
                interaction.activatedAbilityContext() == null
                        ? "Choose any number of cards to reveal from your hand for "
                        : "Choose " + interaction.activatedAbilityContext().xValue()
                                + " cards to reveal from your hand for "
                        + interaction.cardName() + ".");
    }

    private InteractionPromptMessage projectDoomsdayChoice(
            GameData gameData, PendingInteraction.DoomsdayChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                interaction.maxCount(),
                "Choose up to five cards from your library and graveyard to put on top of your "
                        + "library. The rest are exiled.");
    }

    private InteractionPromptMessage projectSearchLibraryAndOrGraveyardChoice(
            GameData gameData, PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction) {
        boolean toBattlefield = interaction.destination() == LibrarySearchDestination.BATTLEFIELD;
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                1,
                "Choose a " + interaction.cardLabel() + " from your library or graveyard to reveal and put it "
                        + (toBattlefield ? "onto the battlefield." : "into your hand."));
    }

    private InteractionPromptMessage projectSearchLibraryToTopChoice(
            GameData gameData, PendingInteraction.SearchLibraryToTopChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                interaction.pool().size(),
                "Choose any number of " + interaction.cardLabel()
                        + " cards to reveal and put on top of your library.");
    }

    private InteractionPromptMessage projectIntuitionSearchChoice(
            GameData gameData, PendingInteraction.IntuitionSearchChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                interaction.count(),
                "Search your library for " + interaction.count()
                        + " cards to reveal. Your opponent chooses one of them for your hand; "
                        + "the rest go into your graveyard.");
    }

    private InteractionPromptMessage projectEcologicalAppreciationSearchChoice(
            GameData gameData, PendingInteraction.EcologicalAppreciationSearchChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                Math.min(4, interaction.pool().size()),
                "Choose up to four creature cards with different names and mana value "
                        + interaction.maxManaValue() + " or less to reveal.");
    }

    private InteractionPromptMessage projectEcologicalAppreciationOpponentChoice(
            GameData gameData, PendingInteraction.EcologicalAppreciationOpponentChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.cards()),
                2,
                "Choose two cards to shuffle into the library. Put the rest onto the battlefield.");
    }

    private InteractionPromptMessage projectVerdantMasterySearchChoice(
            GameData gameData, PendingInteraction.VerdantMasterySearchChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                Math.min(4, interaction.pool().size()),
                "Choose up to four basic land cards to reveal for Verdant Mastery.");
    }

    private InteractionPromptMessage projectVerdantMasteryLandChoice(
            GameData gameData, PendingInteraction.VerdantMasteryLandChoice interaction) {
        if (interaction.chooseForOpponent()) {
            return InteractionPromptMessage.multiCardPick(
                    new ArrayList<>(interaction.validCardIds()),
                    cardViews(interaction.cards()),
                    1,
                    "Choose one basic land card to put onto the battlefield tapped under an opponent's control.");
        }
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.cards()),
                2,
                "Choose two basic land cards to put onto the battlefield tapped under your control.");
    }

    private InteractionPromptMessage projectGuidedPassageChoice(
            GameData gameData, PendingInteraction.GuidedPassageChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.pool()),
                3,
                "Choose a creature card, a land card, and a noncreature, nonland card for Guided Passage.");
    }

    private InteractionPromptMessage projectPermanentAuctionChoice(
            GameData gameData, PendingInteraction.PermanentAuctionChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(),
                cardViews(interaction.pool()),
                1,
                "Choose one of the auctioned cards to put onto the battlefield tapped under "
                        + "your control.");
    }

    private InteractionPromptMessage projectIllicitAuctionBidChoice(
            GameData gameData, PendingInteraction.IllicitAuctionBidChoice interaction) {
        Permanent target = findPermanent(gameData, interaction.targetPermanentId());
        String targetName = target != null ? target.getCard().getName() : "the creature";
        String highBidderName =
                gameData.playerIdToName.getOrDefault(interaction.highBidderId(), "the high bidder");
        String prompt = "Bid life for control of " + targetName + " (current high bid: "
                + interaction.highBid() + " by " + highBidderName + "). Enter more than "
                + interaction.highBid() + " to bid, or " + interaction.highBid()
                + " or less to pass.";
        return InteractionPromptMessage.numberPick(
                prompt, interaction.maxBid(), interaction.cardName());
    }

    private InteractionPromptMessage projectMagesContestBidChoice(
            GameData gameData, PendingInteraction.MagesContestBidChoice interaction) {
        StackEntry target = gameData.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(interaction.targetSpellId()))
                .findFirst()
                .orElse(null);
        String targetName = target != null ? target.getCard().getName() : "the spell";
        String highBidderName =
                gameData.playerIdToName.getOrDefault(interaction.highBidderId(), "the high bidder");
        String prompt = "Bid life to counter " + targetName + " (current high bid: "
                + interaction.highBid() + " by " + highBidderName + "). Enter more than "
                + interaction.highBid() + " to bid, or " + interaction.highBid()
                + " or less to pass.";
        return InteractionPromptMessage.numberPick(
                prompt, interaction.maxBid(), interaction.cardName());
    }

    private InteractionPromptMessage projectPainsRewardBidChoice(
            GameData gameData, PendingInteraction.PainsRewardBidChoice interaction) {
        if (interaction.openingBid()) {
            String prompt = "Choose the opening life bid for " + interaction.cardName()
                    + " (0 through " + interaction.maxBid() + ").";
            return InteractionPromptMessage.numberPick(
                    prompt, interaction.maxBid(), interaction.cardName());
        }
        String highBidderName =
                gameData.playerIdToName.getOrDefault(interaction.highBidderId(), "the high bidder");
        String prompt = "Bid life for " + interaction.cardName() + " (current high bid: "
                + interaction.highBid() + " by " + highBidderName + "). Enter more than "
                + interaction.highBid() + " to bid, or " + interaction.highBid()
                + " or less to pass.";
        return InteractionPromptMessage.numberPick(
                prompt, interaction.maxBid(), interaction.cardName());
    }

    private InteractionPromptMessage projectMultiZoneExileChoice(
            GameData gameData, PendingInteraction.MultiZoneExileChoice interaction) {
        List<CardView> cardViews = new ArrayList<>();
        UUID targetPlayerId = interaction.targetPlayerId();
        addMatchingCardViews(
                cardViews,
                gameData.playerHands.getOrDefault(targetPlayerId, List.of()),
                interaction.validCardIds());
        addMatchingCardViews(
                cardViews,
                gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of()),
                interaction.validCardIds());
        addMatchingCardViews(
                cardViews,
                gameData.playerDecks.getOrDefault(targetPlayerId, List.of()),
                interaction.validCardIds());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose any number of cards named \"" + interaction.cardName() + "\" to exile.");
    }

    private InteractionPromptMessage projectPutUpToCardsFromHandOntoBattlefieldChoice(
            GameData gameData, PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice interaction) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of() : hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose up to " + interaction.maxCount()
                        + " permanent cards from your hand to put onto the battlefield.");
    }

    private InteractionPromptMessage projectPutCardFromHandOrGraveyardChoice(
            GameData gameData, PendingInteraction.PutCardFromHandOrGraveyardChoice interaction) {
        List<CardView> cardViews = new ArrayList<>();
        addMatchingCardViews(cardViews,
                gameData.playerHands.getOrDefault(interaction.playerId(), List.of()),
                interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerGraveyards.getOrDefault(interaction.playerId(), List.of()),
                interaction.validCardIds());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1,
                "Choose up to one " + interaction.label()
                        + " card from your hand or graveyard to put onto the battlefield.");
    }

    private InteractionPromptMessage projectExileNonlandCardFromTargetHandOrGraveyardChoice(
            GameData gameData, PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice interaction) {
        UUID targetPlayerId = interaction.targetPlayerId();
        List<CardView> cardViews = new ArrayList<>();
        addMatchingCardViews(cardViews,
                gameData.playerHands.getOrDefault(targetPlayerId, List.of()), interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of()), interaction.validCardIds());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1,
                "Choose a nonland card from that player's hand or graveyard to exile.");
    }

    private InteractionPromptMessage projectExilePermanentsOrHandCardsChoice(
            GameData gameData, PendingInteraction.ExilePermanentsOrHandCardsChoice interaction) {
        UUID playerId = interaction.playerId();
        List<CardView> cardViews = new ArrayList<>();
        addMatchingCardViews(
                cardViews,
                gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .map(Permanent::getCard)
                        .toList(),
                interaction.validCardIds());
        addMatchingCardViews(
                cardViews,
                gameData.playerHands.getOrDefault(playerId, List.of()),
                interaction.validCardIds());
        int required = Math.min(interaction.count(), interaction.validCardIds().size());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, required,
                interaction.sourceName() + " — exile " + required + " permanent"
                        + (required == 1 ? "" : "s") + " you control and/or cards from your hand.");
    }

    private InteractionPromptMessage projectBeholdChoice(
            GameData gameData, PendingInteraction.BeholdChoice interaction) {
        UUID playerId = interaction.playerId();
        List<CardView> cardViews = new ArrayList<>();
        addMatchingCardViews(cardViews,
                gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .map(Permanent::getCard).toList(), interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerHands.getOrDefault(playerId, List.of()), interaction.validCardIds());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1, interaction.prompt());
    }

    private InteractionPromptMessage projectAttachAurasChoice(
            GameData gameData, PendingInteraction.AttachAurasChoice interaction) {
        UUID playerId = interaction.playerId();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> battlefieldCards = new ArrayList<>();
        gameData.forEachPermanent((owner, permanent) -> battlefieldCards.add(permanent.getCard()));
        addMatchingCardViews(cardViews, battlefieldCards, interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerGraveyards.getOrDefault(playerId, List.of()), interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerHands.getOrDefault(playerId, List.of()), interaction.validCardIds());
        addMatchingCardViews(cardViews,
                gameData.playerDecks.getOrDefault(playerId, List.of()), interaction.validCardIds());
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                interaction.maxCount() == 1
                        ? "Choose an Aura to attach to " + interaction.sourceName() + "."
                        : "Choose any number of Auras to attach to " + interaction.sourceName() + ".");
    }

    private InteractionPromptMessage projectMultiPermanentChoice(
            GameData gameData, PendingInteraction.MultiPermanentChoice interaction) {
        return InteractionPromptMessage.multiPermanentPick(
                new ArrayList<>(interaction.validIds()), new ArrayList<>(interaction.validPlayerIds()),
                interaction.maxCount(), interaction.prompt());
    }

    private InteractionPromptMessage projectMultiGraveyardChoice(
            GameData gameData, PendingInteraction.MultiGraveyardChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(),
                cardViews(interaction.cards()),
                interaction.maxCount(),
                interaction.prompt());
    }

    private InteractionPromptMessage projectColorChoice(
            GameData gameData, PendingInteraction.ColorChoice interaction) {
        return InteractionPromptMessage.listPick(
                interaction.options(),
                interaction.prompt(),
                isCardNameChoice(interaction.context()),
                interaction.disabledOptions());
    }

    private InteractionPromptMessage projectRevealedHandChoice(
            GameData gameData, PendingInteraction.RevealedHandChoice interaction) {
        List<CardView> cardViews =
                cardViews(gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of()));
        return InteractionPromptMessage.cardIndexPick(
                cardViews,
                interaction.validIndices(),
                interaction.prompt(),
                interaction.optional());
    }

    private InteractionPromptMessage projectAlternatingHandExileChoice(
            GameData gameData, PendingInteraction.AlternatingHandExileChoice interaction) {
        List<CardView> cardViews =
                cardViews(gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of()));
        boolean choosingOwnHand = interaction.decidingPlayerId().equals(interaction.targetPlayerId());
        String prompt = choosingOwnHand
                ? "Exile a card from your hand (you will return the cards you exile this way)."
                : "Exile a card from "
                        + gameData.playerIdToName.getOrDefault(interaction.targetPlayerId(), "that player")
                        + "'s hand (it will go to their graveyard).";
        return InteractionPromptMessage.cardIndexPick(
                cardViews, interaction.validIndices(), prompt, false);
    }

    private InteractionPromptMessage projectRevealCardsDiscardChoice(
            GameData gameData, PendingInteraction.RevealCardsDiscardChoice interaction) {
        List<Card> targetHand =
                gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of());
        List<CardView> cardViews;
        if (interaction.revealStage()) {
            cardViews = cardViews(targetHand);
        } else {
            Map<UUID, Card> handById = targetHand.stream()
                    .collect(Collectors.toMap(Card::getId, Function.identity(), (left, right) -> left));
            cardViews = interaction.revealedCardIds().stream()
                    .map(handById::get)
                    .filter(java.util.Objects::nonNull)
                    .map(cardViewFactory::create)
                    .toList();
        }
        return InteractionPromptMessage.cardIndexPick(
                cardViews,
                interaction.validIndices(),
                revealCardsDiscardPrompt(gameData, interaction),
                false);
    }

    private InteractionPromptMessage projectGraveyardChoice(
            GameData gameData, PendingInteraction.GraveyardChoice interaction) {
        return InteractionPromptMessage.graveyardIndexPick(
                interaction.validIndices(), interaction.prompt(), interaction.cardPool() != null);
    }

    private InteractionPromptMessage projectGraveyardExileCostChoice(
            GameData gameData, PendingInteraction.GraveyardExileCostChoice interaction) {
        return InteractionPromptMessage.graveyardIndexPick(
                interaction.validIndices(), interaction.prompt(), false);
    }

    private InteractionPromptMessage projectActivatedAbilityGraveyardExileCostChoice(
            GameData gameData, PendingInteraction.ActivatedAbilityGraveyardExileCostChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                interaction.validCardIds(), cardViews(interaction.cards()), interaction.cards().size(), interaction.prompt());
    }

    private InteractionPromptMessage projectHandChoice(
            PendingInteraction.HandChoice interaction, boolean declinable) {
        return InteractionPromptMessage.cardIndexPick(
                interaction.validIndices(), interaction.prompt(), declinable);
    }

    private InteractionPromptMessage projectPutCardsFromHandOnLibraryCardChoice(
            GameData gameData,
            PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction) {
        String destination = switch (interaction.placement()) {
            case TOP -> "top of";
            case BOTTOM -> "the bottom of";
            case PLAYER_CHOICE -> "top or bottom of";
        };
        String prompt;
        if (interaction.shuffleIn()) {
            prompt = "Choose " + interaction.maxCount() + " card(s) to shuffle into your library.";
        } else if (interaction.swapWithLibraryTop()) {
            prompt = "Choose any number of cards to exile face down and swap for the top of your library.";
        } else {
            prompt = "Choose " + interaction.maxCount() + " card(s) to put on " + destination
                    + " your library.";
        }
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews(interaction.cards()),
                interaction.maxCount(),
                prompt);
    }

    private InteractionPromptMessage projectPutCardsFromHandOnLibraryDestinationChoice(
            GameData gameData,
            PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice interaction) {
        return InteractionPromptMessage.listPick(
                PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.OPTIONS,
                "Put the chosen cards on the top or bottom of your library?",
                false);
    }

    private InteractionPromptMessage projectCounteredSpellLibraryDestinationChoice(
            GameData gameData,
            PendingInteraction.CounteredSpellLibraryDestinationChoice interaction) {
        return InteractionPromptMessage.listPick(
                PendingInteraction.CounteredSpellLibraryDestinationChoice.OPTIONS,
                "Put " + interaction.cardName() + " on the top or bottom of its owner's library?",
                false);
    }

    private InteractionPromptMessage projectTargetLibraryDestinationChoice(
            GameData gameData,
            PendingInteraction.TargetLibraryDestinationChoice interaction) {
        return InteractionPromptMessage.listPick(
                interaction.options(),
                "Put " + interaction.cardName() + " on the " +
                        (interaction.firstOption().equalsIgnoreCase("Top")
                                ? "top"
                                : interaction.firstOption().toLowerCase()) +
                        " or bottom of its owner's library?",
                false);
    }

    private InteractionPromptMessage projectSylvanLibraryChoice(
            GameData gameData, PendingInteraction.SylvanLibraryChoice interaction) {
        List<Card> hand = gameData.playerHands.getOrDefault(interaction.playerId(), List.of());
        Map<UUID, Card> handById = hand.stream()
                .collect(Collectors.toMap(Card::getId, Function.identity(), (left, right) -> left));
        List<UUID> validIds = interaction.drawnThisTurnCardIds().stream()
                .filter(handById::containsKey)
                .toList();
        List<CardView> cardViews = validIds.stream()
                .map(handById::get)
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                validIds,
                cardViews,
                interaction.resolveCount(),
                "Choose up to " + interaction.resolveCount()
                        + " card(s) drawn this turn to put on top of your library. You pay 4 life "
                        + "for each of the " + interaction.resolveCount() + " you don't put back.");
    }

    private InteractionPromptMessage projectLibraryRevealChoice(
            GameData gameData, PendingInteraction.LibraryRevealChoice interaction) {
        if (interaction.prompt() == null) {
            return null;
        }
        Map<UUID, Card> cardsById = interaction.allCards().stream()
                .collect(Collectors.toMap(Card::getId, Function.identity(), (left, right) -> left));
        List<CardView> cardViews = interaction.validCardIds().stream()
                .map(cardsById::get)
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                cardViews,
                interaction.maxCount(),
                interaction.prompt());
    }

    private InteractionPromptMessage projectVividCardChoice(
            GameData gameData, PendingInteraction.VividCardChoice interaction) {
        Map<UUID, Card> cardsById = interaction.revealedCards().stream()
                .collect(Collectors.toMap(Card::getId, Function.identity(), (left, right) -> left));
        List<CardView> cardViews = interaction.validCardIds().stream()
                .map(cardsById::get)
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1, interaction.prompt());
    }

    private InteractionPromptMessage projectNivMizzetColorPairChoice(
            GameData gameData, PendingInteraction.NivMizzetColorPairChoice interaction) {
        Map<UUID, Card> cardsById = interaction.revealedCards().stream()
                .collect(Collectors.toMap(Card::getId, Function.identity(), (left, right) -> left));
        List<CardView> cardViews = interaction.validCardIds().stream()
                .map(cardsById::get)
                .map(cardViewFactory::create)
                .toList();
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews,
                interaction.requiredPairCount(), interaction.prompt());
    }

    private InteractionPromptMessage projectLibrarySearch(
            GameData gameData, PendingInteraction.LibrarySearch interaction) {
        return InteractionPromptMessage.libraryIndexPick(
                cardViews(interaction.params().cards()),
                interaction.messagePrompt(),
                interaction.messageCanFailToFind());
    }

    private InteractionPromptMessage projectSearchOutsideGameOrExileCardChoice(
            GameData gameData, PendingInteraction.SearchOutsideGameOrExileCardChoice interaction) {
        List<CardView> cardViews = new ArrayList<>();
        addMatchingCardViews(cardViews,
                gameData.playerSideboards.getOrDefault(interaction.playerId(), List.of()),
                interaction.validCardIds());
        synchronized (gameData.exiledCards) {
            gameData.exiledCards.stream()
                    .filter(entry -> interaction.playerId().equals(entry.ownerId()) && !entry.faceDown())
                    .filter(entry -> interaction.validCardIds().contains(entry.card().getId()))
                    .map(ExiledCardEntry::card)
                    .map(cardViewFactory::create)
                    .forEach(cardViews::add);
        }
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1,
                "You may reveal a " + interaction.cardLabel()
                        + " from outside the game or choose one in face-up exile.");
    }

    private InteractionPromptMessage projectAssimilationAegisCopyChoice(
            GameData gameData, PendingInteraction.AssimilationAegisCopyChoice interaction) {
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()),
                exiledCardViews(gameData, interaction.validCardIds()),
                1,
                "Choose a creature card exiled with Assimilation Aegis to copy.");
    }

    private InteractionPromptMessage projectFaceUpExiledCardChoice(
            GameData gameData, PendingInteraction.FaceUpExiledCardChoice interaction) {
        List<CardView> cardViews = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            gameData.exiledCards.stream()
                    .filter(entry -> interaction.ownerId().equals(entry.ownerId()) && !entry.faceDown())
                    .filter(entry -> interaction.validCardIds().contains(entry.card().getId()))
                    .map(ExiledCardEntry::card)
                    .map(cardViewFactory::create)
                    .forEach(cardViews::add);
        }
        return InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, 1,
                "You may put a face-up exiled card they own into their graveyard.");
    }

    private InteractionPromptMessage projectPermanentChoice(
            GameData gameData, PendingInteraction.PermanentChoice interaction) {
        return InteractionPromptMessage.permanentPick(
                interaction.validPermanentIds(), interaction.validPlayerIds(), interaction.prompt());
    }

    private InteractionPromptMessage projectAdNauseamRepeatChoice(
            GameData gameData, PendingInteraction.AdNauseamRepeatChoice interaction) {
        return InteractionPromptMessage.acceptDecline(
                "Reveal the next card and lose life equal to its mana value? ("
                        + interaction.sourceName() + ")",
                true,
                null);
    }

    private InteractionPromptMessage projectForbiddenRitualRepeatChoice(
            GameData gameData, PendingInteraction.ForbiddenRitualRepeatChoice interaction) {
        return InteractionPromptMessage.acceptDecline(
                "Sacrifice another nontoken permanent and repeat? ("
                        + interaction.sourceName() + ")",
                true,
                null);
    }

    private InteractionPromptMessage projectExiledPermanentPutOntoBattlefieldChoice(
            GameData gameData, PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice interaction) {
        return InteractionPromptMessage.acceptDecline(
                "Put " + interaction.cardName() + " onto the battlefield? ("
                        + interaction.sourceName() + ")",
                true,
                null);
    }

    private InteractionPromptMessage projectLimDulsVaultRepeatChoice(
            GameData gameData, PendingInteraction.LimDulsVaultRepeatChoice interaction) {
        // The accept/decline shape carries no card list, so the looked-at cards are named in the
        // prompt text; it is delivered only to the deciding player, so the information stays private.
        String names = interaction.lookedAt().stream().map(Card::getName).collect(Collectors.joining(", "));
        boolean canPayLife = gameData.getLife(interaction.playerId()) >= 1;
        return InteractionPromptMessage.acceptDecline(
                "Top of your library: " + names + ". Pay 1 life to put them on the bottom and look at five more?",
                canPayLife,
                null);
    }

    private InteractionPromptMessage projectLimDulsVaultOrderChoice(
            GameData gameData, PendingInteraction.LimDulsVaultOrderChoice interaction) {
        return InteractionPromptMessage.cardOrder(
                cardViews(interaction.cards()),
                interaction.toBottom()
                        ? "Put these cards on the bottom of your library in any order"
                        : "Put these cards on top of your library in any order");
    }

    private AvailableAttackersMessage projectAttackerDeclaration(
            GameData gameData, PendingInteraction.AttackerDeclaration interaction) {
        List<AttackTarget> targets = interaction.availableTargets().stream()
                .map(target -> new AttackTarget(
                        target.id().toString(), target.name(), target.isPlayer()))
                .toList();
        return new AvailableAttackersMessage(
                interaction.attackerIndices(),
                interaction.mustAttackIndices(),
                targets,
                interaction.taxPerCreature(),
                interaction.mustAttackWithAtLeastOne());
    }

    private AvailableBlockersMessage projectBlockerDeclaration(
            GameData gameData, PendingInteraction.BlockerDeclaration interaction) {
        return new AvailableBlockersMessage(
                interaction.blockerIndices(),
                interaction.attackerIndices(),
                interaction.legalBlockPairs(),
                interaction.mustBeBlockedAttackerIndices(),
                interaction.menaceAttackerIndices(),
                interaction.mustBlockRequirements(),
                interaction.choosingForOpponent());
    }

    private CombatDamageAssignmentNotification projectCombatDamageAssignment(
            GameData gameData, PendingInteraction.CombatDamageAssignment interaction) {
        List<CombatDamageTargetView> targetViews = interaction.validTargets().stream()
                .map(target -> new CombatDamageTargetView(
                        target.id().toString(),
                        target.name(),
                        target.effectiveToughness(),
                        target.currentDamage(),
                        target.isPlayer()))
                .toList();
        return new CombatDamageAssignmentNotification(
                interaction.attackerIndex(),
                interaction.attackerPermanentId().toString(),
                interaction.attackerName(),
                interaction.totalDamage(),
                targetViews,
                interaction.isTrample(),
                interaction.isDeathtouch(),
                interaction.singleRecipient());
    }

    private String revealCardsDiscardPrompt(
            GameData gameData, PendingInteraction.RevealCardsDiscardChoice interaction) {
        if (interaction.revealStage()) {
            return interaction.revealedCardIds().isEmpty()
                    ? "Choose " + interaction.remainingCount() + " cards to reveal."
                    : "Choose another card to reveal.";
        }
        String targetName =
                gameData.playerIdToName.getOrDefault(interaction.targetPlayerId(), "that player");
        if (interaction.destination() == HandChoiceDestination.EXILE) {
            return interaction.remainingCount() < interaction.discardCount()
                    ? "Choose another revealed card to exile."
                    : "Choose a revealed card to exile.";
        }
        if (interaction.remainingCount() < interaction.discardCount()) {
            return "Choose another card for " + targetName + " to discard.";
        }
        return interaction.remainingCount() > 1
                ? "Choose " + interaction.remainingCount() + " cards for " + targetName
                        + " to discard."
                : "Choose a card for " + targetName + " to discard.";
    }

    private String describeExiledPile(GameData gameData, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            return "empty";
        }
        return cardIds.stream()
                .map(gameData::findExiledCard)
                .filter(java.util.Objects::nonNull)
                .map(entry -> entry.card().getName())
                .collect(Collectors.joining(", "));
    }

    private String describeCards(List<Card> cards) {
        if (cards.isEmpty()) {
            return "empty";
        }
        return cards.stream().map(Card::getName).collect(Collectors.joining(", "));
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        if (permanentId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanentId.equals(permanent.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<CardView> exiledCardViews(GameData gameData, List<UUID> cardIds) {
        List<CardView> views = new ArrayList<>();
        for (UUID cardId : cardIds) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry != null) {
                views.add(cardViewFactory.create(entry.card()));
            }
        }
        return views;
    }

    private List<CardView> cardViews(List<Card> cards) {
        return cards.stream().map(cardViewFactory::create).toList();
    }

    private void addMatchingCardViews(
            List<CardView> target, List<Card> cards, List<UUID> validCardIds) {
        cards.stream()
                .filter(card -> validCardIds.contains(card.getId()))
                .map(cardViewFactory::create)
                .forEach(target::add);
    }

    private static boolean isCardNameChoice(ChoiceContext context) {
        return context instanceof ChoiceContext.CardNameChoice
                || context instanceof ChoiceContext.ExileByNameChoice
                || context instanceof ChoiceContext.SphinxAmbassadorNameChoice
                || context instanceof ChoiceContext.EachPlayerCardNameRevealChoice
                || context instanceof ChoiceContext.NameCardMillGainLifeChoice
                || context instanceof ChoiceContext.OpponentsCantCastNamedSpellsUntilNextTurnChoice
                || context instanceof ChoiceContext.NameCardMillDrawChoice
                || context instanceof ChoiceContext.TargetPlayerNameCardRevealTopChoice
                || context instanceof ChoiceContext.ChooseNameRevealTopCardsToHandRestToExileChoice
                || context instanceof ChoiceContext.ChooseCardNameRevealTopCardChoice
                || context instanceof ChoiceContext.AssemblyHallCreatureCardChoice;
    }

    private <T extends PendingInteraction> void register(
            Class<T> interactionType, ProjectionStrategy<T> strategy) {
        ProjectionStrategy<?> previous = strategies.putIfAbsent(interactionType, strategy);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate interaction prompt projection for " + interactionType.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private ProjectionStrategy<PendingInteraction> strategyFor(PendingInteraction interaction) {
        ProjectionStrategy<?> strategy = strategies.get(interaction.getClass());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No interaction prompt projection registered for "
                            + interaction.getClass().getName());
        }
        return (ProjectionStrategy<PendingInteraction>) strategy;
    }

    @FunctionalInterface
    private interface ProjectionStrategy<T extends PendingInteraction> {
        Object project(GameData gameData, T interaction);
    }
}
