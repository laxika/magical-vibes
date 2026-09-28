package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.v.VhalEagerScholar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeVhalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies Vhal, Eager Scholar's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeVhalEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeVhalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeVhalEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Vhal, Eager Scholar".equals(source.getCard().getName())) {
            return;
        }

        int removedStudyCounters = source.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.STUDY);
        source.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.STUDY, 0);

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        VhalEagerScholar.setSpecializedBaseCharacteristics(specialized);
        VhalEagerScholar.addLootAbility(specialized);
        specialized.setPower(4);
        specialized.setToughness(4);

        CardEffect specializedTrigger = VhalEagerScholar.specializedTrigger(specialize.color());
        setFaceCharacteristics(specialized, specialize.color());
        installSpecializedTrigger(specialized, specialize.color(), specializedTrigger);
        source.exchangeCard(specialized);

        if (specialize.color() == CardColor.BLUE || removedStudyCounters > 0) {
            if (specialized.getSpellTargets().isEmpty()) {
                enqueueNonTargetingTrigger(gameData, entry, specialized, specializedTrigger,
                        removedStudyCounters);
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        specialized,
                        entry.getControllerId(),
                        List.of(specializedTrigger),
                        entry.getSourcePermanentId(),
                        List.of(),
                        0,
                        0,
                        List.of(),
                        specialize.color() == CardColor.BLACK ? removedStudyCounters : 0,
                        List.of(),
                        false,
                        null,
                        null,
                        removedStudyCounters));
            }
        }
    }

    private void installSpecializedTrigger(Card specialized, CardColor color, CardEffect trigger) {
        switch (color) {
            case WHITE -> specialized.targetUpTo(new com.github.laxika.magicalvibes.model.amount.EventValue(),
                            TargetFilters.creature(), 100)
                    .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, trigger);
            case BLACK -> specialized.target(
                            new GraveyardCardPredicateTargetFilter(
                                    new CardAllOfPredicate(List.of(
                                            new CardTypePredicate(CardType.CREATURE),
                                            new CardMaxManaValueXPredicate())),
                                    GraveyardSearchScope.ALL_GRAVEYARDS))
                    .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, trigger);
            case RED -> specialized.target(new PermanentPredicateTargetFilter(
                            VhalEagerScholar.creatureOrPlaneswalkerAnOpponentControls(),
                            "Target must be a creature or planeswalker an opponent controls"))
                    .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, trigger);
            default -> specialized.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, trigger);
        }
    }

    private void enqueueNonTargetingTrigger(GameData gameData, StackEntry sourceEntry,
                                            Card sourceCard, CardEffect effect, int eventValue) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sourceEntry.getControllerId(),
                sourceCard.getName() + "'s ability",
                List.of(effect),
                0,
                sourceEntry.getSourcePermanentId());
        trigger.setEventValue(eventValue);
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Vhal, Scholar of Tactics");
                card.setManaCost("{2}{W}{U}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, distribute that many +1/+1 counters among any number of target creatures.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case BLUE -> {
                card.setName("Vhal, Scholar of Prophecy");
                card.setManaCost("{2}{U}{U}");
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "Look at that many cards from the top of your library. Put one of those cards into your hand "
                        + "and the rest on the bottom of your library in a random order.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case BLACK -> {
                card.setName("Vhal, Scholar of Mortality");
                card.setManaCost("{2}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLACK);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, put target creature card with mana value less than or equal to the number "
                        + "of study counters removed this way from a graveyard onto the battlefield.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case RED -> {
                card.setName("Vhal, Scholar of Elements");
                card.setManaCost("{2}{U}{R}");
                card.setColors(List.of(CardColor.BLUE, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, Vhal, Scholar of Elements deals that much damage to target creature "
                        + "or planeswalker an opponent controls.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case GREEN -> {
                card.setName("Vhal, Scholar of Creation");
                card.setManaCost("{2}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, seek two creature cards with mana value less than or equal to the number "
                        + "of study counters removed this way. Put one of them onto the battlefield and shuffle the "
                        + "other into your library.\n{T}: Draw a card, then discard a card.");
            }
            default -> throw new IllegalStateException("Unsupported Vhal specialization color: " + color);
        }
    }
}
