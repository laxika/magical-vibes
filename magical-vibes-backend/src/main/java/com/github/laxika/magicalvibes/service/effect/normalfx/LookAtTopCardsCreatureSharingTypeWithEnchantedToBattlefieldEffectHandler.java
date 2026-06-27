package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayExileOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardRemoveTargetFromCombatIfMatchEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOpponentPaysLifeOrToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsTypeToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ScryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect e = (LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect) effect;

        // Find the source aura permanent and the enchanted creature
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            log.warn("Game {} - No source permanent for Call to the Kindred effect", gameData.id);
            return;
        }
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (auraPerm == null || !auraPerm.isAttached()) {
            log.info("Game {} - Aura no longer on battlefield or not attached, effect does nothing", gameData.id);
            return;
        }
        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, auraPerm.getAttachedTo());
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, effect does nothing", gameData.id);
            return;
        }

        // Collect the enchanted creature's creature subtypes (including transient subtypes)
        List<CardSubtype> enchantedTypes = new ArrayList<>(enchantedCreature.getCard().getSubtypes());
        enchantedTypes.addAll(enchantedCreature.getTransientSubtypes());
        boolean enchantedIsChangeling = enchantedCreature.hasKeyword(Keyword.CHANGELING);

        if (enchantedTypes.isEmpty() && !enchantedIsChangeling) {
            // Enchanted creature has no creature types — no card can share a type
            LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
            if (result == null) return;
            libraryRevealSupport.reorderRemainingToBottom(gameData, result.controllerId(), result.topCards());
            return;
        }

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        // Filter for creature cards that share a creature type with the enchanted creature
        List<Card> matchingCards = topCards.stream()
                .filter(card -> card.getType() == CardType.CREATURE
                        || card.getAdditionalTypes().contains(CardType.CREATURE))
                .filter(card -> {
                    List<CardSubtype> cardTypes = card.getSubtypes();
                    boolean cardIsChangeling = card.getKeywords().contains(Keyword.CHANGELING);

                    return (enchantedIsChangeling && (cardIsChangeling || !cardTypes.isEmpty()))
                            || (cardIsChangeling && !enchantedTypes.isEmpty())
                            || enchantedTypes.stream().anyMatch(cardTypes::contains);
                })
                .toList();

        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(controllerId, matchingCards)
                .canFailToFind(true)
                .sourceCards(topCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("You may put a creature card that shares a creature type with the enchanted creature onto the battlefield.")
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .build());

        List<CardView> cardViews = matchingCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "You may put a creature card that shares a creature type with the enchanted creature onto the battlefield.",
                true
        ));
    
    }
}
