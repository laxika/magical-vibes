package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExile;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExileWithCounter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDarigaazShivanChampionSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Darigaaz, Shivan Champion's random Dragon spellbook. */
@Component
@RequiredArgsConstructor
public class ConjureDarigaazShivanChampionSpellbookEffectHandler implements NormalEffectHandlerBean {

    /**
     * Shivan Dragon, Moonveil Regent, Terror of the Peaks, Leyline Tyrant, Immersturm Predator, Manaform Hellkite,
     * Bone Dragon, Demanding Dragon, Skarrgan Hellkite, Thunderbreak Regent, Black Dragon, Skyship Stalker and Red
     * Dragon, each by a printing of a core set or an expansion.
     */
    static final List<SpellbookCard> SPELLBOOK = List.of(
            new SpellbookCard("10E", "230"),
            new SpellbookCard("MID", "149"),
            new SpellbookCard("M21", "164"),
            new SpellbookCard("ZNR", "147"),
            new SpellbookCard("KHM", "214"),
            new SpellbookCard("VOW", "170"),
            new SpellbookCard("M19", "88"),
            new SpellbookCard("M19", "135"),
            new SpellbookCard("RNA", "114"),
            new SpellbookCard("DTK", "162"),
            new SpellbookCard("AFR", "90"),
            new SpellbookCard("KLD", "130"),
            new SpellbookCard("AFR", "160"));

    private final GameLogService gameLogService;
    private final CardCatalog cardCatalog;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDarigaazShivanChampionSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getControllerId();
        Card card = createSpellbookCard(SPELLBOOK.get(ThreadLocalRandom.current().nextInt(SPELLBOOK.size())), ownerId);

        gameData.addToExile(ownerId, card, null, true);
        gameData.setExiledCardCounters(card.getId(), CounterType.EGG, 3);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a random card from its spellbook into exile face down with three egg counters."));
        ActionEvents.conjured(gameData, ownerId, 1);
    }

    private Card createSpellbookCard(SpellbookCard printing, UUID ownerId) {
        Card card = cardCatalog.createCard(printing);
        card.setOwnerId(ownerId);
        card.setConjured(true);
        // It gains "At the beginning of your upkeep, if this card is exiled, remove an egg counter from it. Then if it has
        // no egg counters on it, put it onto the battlefield."
        card.addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExile()),
                SequenceEffect.of(
                        new RemoveCountersFromExiledCardsEffect(Subjects.sourceCard(), CounterType.EGG),
                        new ConditionalEffect(new NotCondition(new SourceCardInExileWithCounter(CounterType.EGG)),
                                new PutCardsOntoBattlefieldEffect(Subjects.sourceCard()))));
        card.freeze();
        return card;
    }
}
