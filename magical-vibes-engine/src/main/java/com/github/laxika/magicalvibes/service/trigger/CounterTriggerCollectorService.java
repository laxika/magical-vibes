package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Collects triggers caused by a spell being countered by a spell or ability a player controls, and a spell's own
 * "whenever this spell is countered or fizzles".
 */
@Service
public class CounterTriggerCollectorService {

    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_COUNTERS_SPELL)
    private boolean handleControllerCountersSpell(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SpellCountered countered = (TriggerContext.SpellCountered) ctx;
        enqueueTrigger(match, countered.counteringPlayerId());
        return true;
    }

    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_SPELL_COUNTERED)
    private boolean handleControllerSpellCountered(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SpellCastCountered countered = (TriggerContext.SpellCastCountered) ctx;
        enqueueTrigger(match, countered.spellControllerId());
        return true;
    }

    /**
     * "Whenever this spell is countered or fizzles" (Trial and Error): the spell's own ability goes on the stack as the
     * card wrote it, controlled by the spell's controller, with the spell as it last existed on the stack as its
     * triggering spell ("copy it" copies that, CR 608.2h).
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_SELF_SPELL_COUNTERED_OR_FIZZLED)
    private boolean handleSelfSpellCounteredOrFizzled(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SpellCounteredOrFizzled countered = (TriggerContext.SpellCounteredOrFizzled) ctx;
        StackEntry spell = countered.spellEntry();
        if (spell == null || spell.getCard() == null) return false;

        var spellCard = spell.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                spellCard,
                match.controllerId(),
                spellCard.getName() + "'s ability",
                match.stackEffects());
        entry.setTriggeringCardId(spellCard.getId());
        entry.setTriggeringSpell(spell);
        match.gameData().enqueueTrigger(entry);
        return true;
    }

    private void enqueueTrigger(TriggerMatchContext match, UUID controllerId) {
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                controllerId,
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        gameData.enqueueTrigger(entry);
    }
}
