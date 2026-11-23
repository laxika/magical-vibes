package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Reduces equip abilities targeting the permanent the source Aura or Equipment is attached to. Reconfigure is not an
 * equip ability (CR 702.151a, Strong Back's ruling), so it is not reduced.
 */
public record ReduceEquipCostForTargetingAttachedPermanentEffect(int amount)
        implements AttachedPermanentActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate EQUIPMENT =
            new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);

    @Override
    public PermanentPredicate affectedPermanents() {
        return EQUIPMENT;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return false;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability, Permanent reducingPermanent,
                             UUID targetId, List<UUID> targetIds) {
        if (ability == null || reducingPermanent == null
                || reducingPermanent.getAttachedTo() == null
                || ability.getEffects().stream().noneMatch(effect ->
                        effect instanceof EquipEffect equip && !equip.permitsCreatureEquipment())) {
            return false;
        }
        UUID attachedId = reducingPermanent.getAttachedTo();
        return attachedId.equals(targetId) || targetIds != null && targetIds.contains(attachedId);
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }
}
