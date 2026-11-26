package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The AI's answer to a {@link ChoosePermanentsEffect}: its own strongest where the steps after the choice keep or
 * improve what is chosen (spare it from a wipe or a can't-block rule, tap it for a reward, ...), the opponents' strongest otherwise, its own
 * weakest when it must choose among its own (an edict it is the target of), as many as must be chosen, no two sharing
 * a quality that must differ.
 */
final class ChosenPermanentsAiChoice {

    private ChosenPermanentsAiChoice() {
    }

    static List<UUID> choose(AiInteractionContext ctx, ChoosePermanentsEffect choice, List<UUID> validIds,
                             int minimum, int maximum) {
        GameData gameData = ctx.gameData();
        boolean favorsChosen = favorsChosen(gameData.pendingEffectResolutionEntry);
        List<Permanent> own = new ArrayList<>();
        List<Permanent> others = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> side = playerId.equals(ctx.aiPlayerId()) ? own : others;
            battlefield.stream().filter(permanent -> validIds.contains(permanent.getId())).forEach(side::add);
        });
        // By power, then by mana value among those without any (an artifact, a land); a face-down one's is 0.
        Comparator<Permanent> byStrength = Comparator
                .<Permanent>comparingInt(permanent -> ctx.gameQueryService().getEffectivePower(gameData, permanent))
                .thenComparingInt(permanent -> ctx.gameQueryService().getPermanentManaValue(permanent));
        List<Permanent> preferred = favorsChosen ? own : others;
        List<Permanent> rest = favorsChosen ? others : own;
        preferred.sort(byStrength.reversed());
        rest.sort(byStrength);

        List<UUID> chosen = new ArrayList<>();
        Set<Integer> values = new HashSet<>();
        for (Permanent permanent : preferred) {
            if (chosen.size() < maximum && (choice.different() == null || values.add(switch (choice.different()) {
                case POWER -> ctx.gameQueryService().getEffectivePower(gameData, permanent);
                case MANA_VALUE -> ctx.gameQueryService().getPermanentManaValue(permanent);
            }))) {
                chosen.add(permanent.getId());
            }
        }
        for (Permanent permanent : rest) {
            if (chosen.size() < minimum) {
                chosen.add(permanent.getId());
            }
        }
        return chosen;
    }

    private static boolean favorsChosen(StackEntry resolving) {
        if (resolving == null) {
            return false;
        }
        PermanentSubject chosen = Subjects.thisWay(StepKind.CHOOSE);
        for (CardEffect effect : resolving.getEffectsToResolve()) {
            boolean favors = switch (effect) {
                case DestroyAllPermanentsEffect wipe -> chosen.equals(wipe.spared());
                case CantBlockThisTurnEffect restriction -> restriction.subject() instanceof PermanentSubject.Except except
                        && chosen.equals(except.excluded());
                case GrantKeywordEffect grant -> chosen.equals(grant.subject());
                case CreateTokenCopyOfTargetPermanentEffect copy -> chosen.equals(copy.subject());
                case PutCountersEffect counters -> chosen.equals(counters.subject())
                        && counters.counterType() == CounterType.PLUS_ONE_PLUS_ONE;
                case AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect attach ->
                        chosen.equals(attach.subject());
                case BoostEffect boost -> chosen.equals(boost.subject())
                        && !(boost.powerBoost() instanceof Fixed fixed && fixed.value() < 0);
                // "Tap any number of untapped creatures you control. [Something] for each creature tapped this way."
                case TapPermanentsEffect tap -> chosen.equals(tap.subject());
                // "Remove a lore counter from each of any number of Sagas you control": the chapters come again.
                case RemoveCountersEffect remove -> chosen.equals(remove.subject())
                        && remove.counterType() == CounterType.LORE;
                default -> false;
            };
            if (favors) {
                return true;
            }
        }
        return false;
    }
}
