package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Puts the reflexive ability a mana ability's card writes after its mana ("Add {C}. When you do, mill two cards";
 * "Add {R}. When you do, this creature deals 1 damage to each opponent") on the stack. A triggered ability that
 * can't add mana is not a mana ability, so it waits for the stack (CR 603.12, CR 605.5a) instead of resolving with
 * the mana. The mana ability has no stack entry, so the ability records no event.
 */
@Component
public class ReflexiveManaAbilityRiderEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        QueueReflexiveAbilityEffect reflexive = (QueueReflexiveAbilityEffect) effect;
        StackEntry ability = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                permanent.getCard(), playerId, permanent.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(reflexive.effect())), null, permanent.getId());
        ability.setSourcePermanentSnapshot(new Permanent(permanent));
        gameData.enqueueTrigger(ability);
    }
}
