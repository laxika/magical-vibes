package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.Objects;
import java.util.UUID;

/**
 * Trigger-only wrapper for an effect that watches creatures becoming targets of an ability from a
 * particular land. The land name and type, its controller, and the watched permanent are checked
 * when the trigger is collected; the wrapped effect is then placed on the stack.
 */
public record TriggeringAbilityFromNamedLandConditionalEffect(
        String landName,
        CardEffect wrapped
) implements CardEffect {

    @Override
    public CardEffect resolveForBecomesTargetOfSpellOrAbility(
            StackEntry triggeringEntry,
            UUID watcherPermanentId,
            UUID targetedPermanentId,
            UUID watcherControllerId,
            UUID triggeringSourceControllerId) {
        if (triggeringEntry == null
                || (triggeringEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                || triggeringEntry.getSourcePermanentId() == null
                || triggeringEntry.getCard() == null
                || !Objects.equals(landName, triggeringEntry.getCard().getName())
                || !triggeringEntry.getCard().hasType(CardType.LAND)
                || !Objects.equals(watcherControllerId, triggeringSourceControllerId)
                || Objects.equals(watcherPermanentId, targetedPermanentId)) {
            return null;
        }
        return wrapped;
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
