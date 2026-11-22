package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Makes the source a copy of what the subject names, keeping the ability that did it when the text says so. */
@Component
@RequiredArgsConstructor
public class BecomeCopyOfSubjectEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfSubjectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var become = (BecomeCopyOfSubjectEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        Card copied = copied(gameData, entry, become);
        if (copied == null) {
            return;
        }
        boolean untilEndOfTurn = become.duration().equals(Duration.UNTIL_END_OF_TURN);
        Card preCopyCard = untilEndOfTurn ? PermanentCopierService.preCopyCard(gameData, source) : null;
        // "Except it has this ability" keeps only this ability, wherever the permanent got it (printed, or copied from
        // another such permanent): read from the card before the copy replaces it.
        Card asItWas = source.getCard();
        List<ActivatedAbility> keptActivated = !become.keepsThisAbility() ? List.of()
                : asItWas.getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .filter(ability -> ability.getEffects().stream().anyMatch(part -> contains(part, become)))
                .toList();
        List<TriggeredAbility> keptTriggered = !become.keepsThisAbility() ? List.of()
                : asItWas.getTriggeredAbilities().stream()
                .filter(ability -> ability.effects().stream().map(EffectRegistration::effect)
                        .anyMatch(part -> contains(part, become)))
                .toList();
        String originalName = FaceDownVisibility.publicName(gameData, asItWas);
        permanentCopierService.applyCloneCopy(source, copied, null, null, Set.of(), keptActivated);
        keptTriggered.forEach(source.getCard()::addTriggeredAbility);
        if (untilEndOfTurn) {
            // The copy has a new set of abilities, so a once-each-turn ability it now has has not triggered yet.
            gameData.keyedOncePerTurnTriggersFiredThisTurn.remove(source.getId());
            PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), source.getId(),
                    entry.getControllerId(), source, preCopyCard, Duration.UNTIL_END_OF_TURN);
        }
        gameLogService.append(gameData, GameLog.text(originalName + " becomes a copy of " + copied.getName()
                + (untilEndOfTurn ? " until end of turn." : ".")));
    }

    private Card copied(GameData gameData, StackEntry entry, BecomeCopyOfSubjectEffect become) {
        if (become.card() != null) {
            List<Card> cards = subjectResolutionService.cards(gameData, entry, become, become.card());
            return cards.isEmpty() ? null : cards.getFirst();
        }
        for (UUID id : subjectResolutionService.permanentIds(gameData, entry, become, become.permanent())) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                permanent = ConditionEvaluationService.lastKnownPermanent(gameData, id);
            }
            if (permanent != null) {
                return permanent.isFaceDown() ? permanentCopierService.copiableCard(permanent) : permanent.getCard();
            }
        }
        return null;
    }

    private static boolean contains(CardEffect effect, BecomeCopyOfSubjectEffect become) {
        return switch (effect) {
            case BecomeCopyOfSubjectEffect same -> same.equals(become);
            case SequenceEffect sequence -> sequence.steps().stream().anyMatch(step -> contains(step, become));
            case ConditionalEffect conditional -> contains(conditional.wrapped(), become);
            case MayEffect may -> contains(may.wrapped(), become);
            case null, default -> false;
        };
    }
}
