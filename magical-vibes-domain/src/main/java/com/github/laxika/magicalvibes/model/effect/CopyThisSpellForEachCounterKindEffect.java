package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger descriptor for copying a spell once for each concrete counter kind among permanents
 * controlled by the spell's caster.
 */
public record CopyThisSpellForEachCounterKindEffect() implements SpellCastCopyTriggerEffect {

    @Override
    public int copyCount(GameData gameData, UUID castingPlayerId) {
        if (gameData == null || castingPlayerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(castingPlayerId);
        if (battlefield == null) return 0;

        Set<CounterType> counterKinds = EnumSet.noneOf(CounterType.class);
        for (Permanent permanent : battlefield) {
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && permanent.getCounterCount(counterType) > 0) {
                    counterKinds.add(counterType);
                }
            }
        }
        return counterKinds.size();
    }

    @Override
    public boolean tokenCopy() {
        return false;
    }
}
