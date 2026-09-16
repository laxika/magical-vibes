package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellWhenCounteredOrFizzlesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Collects triggers caused by a spell being countered by a spell or ability a player controls. */
@Service
public class CounterTriggerCollectorService {

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_COUNTERS_SPELL)
    private boolean handleControllerCountersSpell(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SpellCountered countered = (TriggerContext.SpellCountered) ctx;
        enqueueTrigger(match, effect, countered.counteringPlayerId());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_SPELL_COUNTERED)
    private boolean handleControllerSpellCountered(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SpellCastCountered countered = (TriggerContext.SpellCastCountered) ctx;
        enqueueTrigger(match, effect, countered.spellControllerId());
        return true;
    }

    @CollectsTrigger(value = CopyThisSpellWhenCounteredOrFizzlesEffect.class,
            slot = EffectSlot.ON_SELF_SPELL_COUNTERED_OR_FIZZLED)
    private boolean handleSelfSpellCounteredOrFizzled(TriggerMatchContext match,
            CopyThisSpellWhenCounteredOrFizzlesEffect effect, TriggerContext ctx) {
        TriggerContext.SpellCounteredOrFizzled countered = (TriggerContext.SpellCounteredOrFizzled) ctx;
        StackEntry spellSnapshot = countered.spellEntry();
        if (spellSnapshot == null || spellSnapshot.getCard() == null) return false;

        var spellCard = spellSnapshot.getCard();
        CardEffect copyEffect = new MayEffect(
                new CopyControllerCastSpellEffect(new StackEntry(spellSnapshot), spellSnapshot.getControllerId()),
                "Copy " + spellCard.getName() + "?");
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                spellCard,
                spellSnapshot.getControllerId(),
                spellCard.getName() + "'s ability",
                new ArrayList<>(List.of(copyEffect))));
        return true;
    }

    private void enqueueTrigger(TriggerMatchContext match, CardEffect effect, UUID controllerId) {
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                controllerId,
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        gameData.enqueueTrigger(entry);
    }
}
