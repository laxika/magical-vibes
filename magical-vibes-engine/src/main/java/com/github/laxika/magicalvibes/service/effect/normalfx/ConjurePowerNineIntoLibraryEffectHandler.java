package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ConjurePowerNineIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Oracle of the Alpha's Power Nine conjure ability. */
@Component
@RequiredArgsConstructor
public class ConjurePowerNineIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjurePowerNineIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        library.addAll(List.of(
                createBlackLotus(controllerId),
                createMoxPearl(controllerId),
                createMoxSapphire(controllerId),
                createMoxRuby(controllerId),
                createMoxJet(controllerId),
                createMoxEmerald(controllerId),
                createAncestralRecall(controllerId),
                createTimeWalk(controllerId),
                createTimetwister(controllerId)));
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures the Power Nine into their library, then shuffles."));
    }

    private Card createBlackLotus(UUID ownerId) {
        Card card = tokenCard(ownerId, "Black Lotus", CardType.ARTIFACT, "{0}", null,
                "{T}, Sacrifice Black Lotus: Add three mana of any one color.");
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "{T}, Sacrifice this artifact: Add three mana of any one color."
        ));
        return freeze(card);
    }

    private Card createMoxPearl(UUID ownerId) {
        Card card = tokenCard(ownerId, "Mox Pearl", CardType.ARTIFACT, "{0}", null,
                "{T}: Add {W}.");
        card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        return freeze(card);
    }

    private Card createMoxSapphire(UUID ownerId) {
        Card card = tokenCard(ownerId, "Mox Sapphire", CardType.ARTIFACT, "{0}", null,
                "{T}: Add {U}.");
        card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        return freeze(card);
    }

    private Card createMoxRuby(UUID ownerId) {
        Card card = tokenCard(ownerId, "Mox Ruby", CardType.ARTIFACT, "{0}", null,
                "{T}: Add {R}.");
        card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        return freeze(card);
    }

    private Card createMoxJet(UUID ownerId) {
        Card card = tokenCard(ownerId, "Mox Jet", CardType.ARTIFACT, "{0}", null,
                "{T}: Add {B}.");
        card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        return freeze(card);
    }

    private Card createMoxEmerald(UUID ownerId) {
        Card card = tokenCard(ownerId, "Mox Emerald", CardType.ARTIFACT, "{0}", null,
                "{T}: Add {G}.");
        card.addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        return freeze(card);
    }

    private Card createAncestralRecall(UUID ownerId) {
        Card card = tokenCard(ownerId, "Ancestral Recall", CardType.INSTANT, "{U}", CardColor.BLUE,
                "Target player draws three cards.");
        card.target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect(3, false, true));
        return freeze(card);
    }

    private Card createTimeWalk(UUID ownerId) {
        Card card = tokenCard(ownerId, "Time Walk", CardType.SORCERY, "{1}{U}", CardColor.BLUE,
                "Take an extra turn after this one.");
        card.addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        return freeze(card);
    }

    private Card createTimetwister(UUID ownerId) {
        Card card = tokenCard(ownerId, "Timetwister", CardType.SORCERY, "{2}{U}", CardColor.BLUE,
                "Each player shuffles their hand and graveyard into their library, then draws seven cards.");
        card.addEffect(EffectSlot.SPELL, new EachPlayerShufflesZonesIntoLibraryEffect());
        card.addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
        return freeze(card);
    }

    private Card tokenCard(UUID ownerId, String name, CardType type, String manaCost,
                           CardColor color, String cardText) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        if (color != null) {
            card.setColors(List.of(color));
        }
        card.setCardText(cardText);
        card.setOwnerId(ownerId);
        card.setToken(true);
        card.setTokenCard(true);
        return card;
    }

    private Card freeze(Card card) {
        card.freeze();
        return card;
    }
}
