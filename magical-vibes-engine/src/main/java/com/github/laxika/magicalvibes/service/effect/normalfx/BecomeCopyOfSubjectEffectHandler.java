package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Makes each copier a copy of what the subject names, with the copy's exceptions as part of its copiable values
 * (CR 707.9b), keeping the ability that did it when the text says so. A lasting copy is a layer-1 floating effect
 * (CR 613.2a) its duration clock ends, the newest copy left applying again (CR 613.7).
 */
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
        List<Permanent> copiers = subjectResolutionService.permanents(gameData, entry, become, become.copier()).stream()
                .filter(copier -> durationHolds(gameData, entry, become.duration(), copier))
                .toList();
        if (copiers.isEmpty()) {
            return;
        }
        Card copied = copied(gameData, entry, become);
        if (copied == null) {
            return;
        }
        String copiedName = copied.getName() == null || copied.getName().isEmpty()
                ? "a nameless permanent" : copied.getName();
        for (Permanent copier : copiers) {
            becomeCopy(gameData, entry, become, copier, copied, copiedName);
        }
    }

    private void becomeCopy(GameData gameData, StackEntry entry, BecomeCopyOfSubjectEffect become, Permanent copier,
                            Card copied, String copiedName) {
        boolean lasting = !become.duration().equals(Duration.PERMANENT);
        Card preCopyCard = lasting ? PermanentCopierService.preCopyCard(gameData, copier) : null;
        // "Except it has this ability" keeps only this ability, wherever the permanent got it (printed, or copied from
        // another such permanent): read from the card before the copy replaces it.
        Card asItWas = copier.getCard();
        List<ActivatedAbility> keptActivated = !become.keepsThisAbility() ? List.of()
                : asItWas.getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .filter(ability -> ability.getEffects().stream().anyMatch(part -> contains(part, become)))
                .toList();
        List<TriggeredAbility> keptTriggered = !become.keepsThisAbility() ? List.of()
                : asItWas.getTriggeredAbilities().stream()
                .filter(ability -> ability.effects().stream().map(EffectRegistration::effect)
                        .anyMatch(part -> contains(part, become)))
                .toList();
        String originalName = FaceDownVisibility.publicName(gameData, copier);
        permanentCopierService.applyCloneCopy(copier, copied, null, null, become.exceptions().addedCardTypes(),
                keptActivated);
        keptTriggered.forEach(copier.getCard()::addTriggeredAbility);
        Set<Keyword> addedKeywords = become.exceptions().addedKeywords();
        if (!addedKeywords.isEmpty()) {
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(copier.getCard().getKeywords());
            keywords.addAll(addedKeywords);
            copier.getCard().setKeywords(keywords);
        }
        if (lasting) {
            // The copy has a new set of abilities, so a once-each-turn ability it now has has not triggered yet.
            gameData.keyedOncePerTurnTriggersFiredThisTurn.remove(copier.getId());
            PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), copier, preCopyCard, become.duration());
        }
        gameLogService.append(gameData, GameLog.text(originalName + " becomes a copy of " + copiedName
                + durationText(become.duration()) + "."));
    }

    /**
     * Whether the copy's duration has not already ended as the effect would begin (CR 611.2b): "for as long as this
     * Equipment remains attached to it" needs the source on the battlefield attached to the copier.
     */
    private boolean durationHolds(GameData gameData, StackEntry entry, Duration duration, Permanent copier) {
        if (duration.asLongAs() == null || !duration.asLongAs().needsAttachment()) {
            return true;
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null && source.isAttached() && copier.getId().equals(source.getAttachedTo());
    }

    private static String durationText(Duration duration) {
        if (duration.equals(Duration.UNTIL_END_OF_TURN)) {
            return " until end of turn";
        }
        if (duration.equals(Duration.UNTIL_YOUR_NEXT_TURN)) {
            return " until its controller's next turn";
        }
        return "";
    }

    private Card copied(GameData gameData, StackEntry entry, BecomeCopyOfSubjectEffect become) {
        if (become.card() != null) {
            List<Card> cards = subjectResolutionService.cards(gameData, entry, become, become.card());
            return cards.isEmpty() ? null : cards.getFirst();
        }
        for (UUID id : subjectResolutionService.permanentIds(gameData, entry, become, become.permanent())) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            // Its copiable values (CR 707.2): a face-down one's are those its face-down status lists (CR 708.2a);
            // "that creature" after it left: as it last existed on the battlefield (CR 608.2h).
            Card card = permanent != null
                    ? permanentCopierService.copiableCard(permanent)
                    : permanentCopierService.lastKnownCopiableCard(gameData, entry, id);
            if (card != null) {
                return card;
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
