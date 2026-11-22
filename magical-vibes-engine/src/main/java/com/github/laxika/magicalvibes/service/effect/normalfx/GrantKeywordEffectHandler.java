package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantKeywordEffect linkedGrant = (GrantKeywordEffect) effect;
        if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(linkedGrant.duration())
                && (entry.getSourcePermanentId() == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(
                        gameData, entry.getSourcePermanentId())))) {
            return;
        }
        var grant = (GrantKeywordEffect) effect;
        PermanentSubject subject = grant.subject();
        if (subject.isSet()) {
            resolveSet(gameData, entry, grant);
            return;
        }

        if (subject instanceof PermanentSubject.TriggeringSpellTargets) {
            grantToEach(gameData, entry, grant, subjectResolutionService.permanentIds(gameData, entry, effect, subject),
                    "target creature(s)");
            return;
        }

        if (subject instanceof PermanentSubject.TriggeringAttackers) {
            // "They gain …" of a "whenever you attack" ability: the attacking creatures the entry recorded as the
            // ability triggered, wherever they are now.
            grantToEach(gameData, entry, grant, subjectResolutionService.permanentIds(gameData, entry, effect, subject),
                    "attacking creature(s)");
            return;
        }

        if (subject instanceof PermanentSubject.WithCreaturesSharingAColor) {
            resolveTargetAndSharingCreatures(gameData, entry, grant);
            return;
        }

        if (subject instanceof PermanentSubject.Attached) {
            // An "enchanted creature" subject names the host only while it is a creature.
            Permanent target = subjectResolutionService.permanent(gameData, entry, effect, subject);
            if (target == null) {
                return;
            }
            if (grant.grantCondition() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                    target, grant.grantCondition(), resolutionFilterContext(gameData, entry))) {
                return;
            }

            Set<Keyword> grantableKeywords = grantableKeywords(gameData, target, grant.keywords());
            if (grantableKeywords.isEmpty()) {
                return;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, Subjects.target(), null,
                            grant.duration(), grant.grantCondition()),
                    target.getId(), null, null, grant.duration(), 0));

            String keywordNames = formatKeywords(grant.keywords());
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" gains " + keywordNames + " " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(),
                    grant.keywords(), subject);
            return;
        }

        // "It gains haste" after a spell's "put a card onto the battlefield, then": the put handler records the
        // permanent as the entry's target, and the spell has no source permanent (Terminal Velocity). A target may
        // cover several permanents when the effect is bound to a target group (Blades of Velis Vel: "up to two
        // target creatures"); the bandmates and the tokens just created are several too.
        PermanentSubject recipientsSubject = subject instanceof PermanentSubject.Source
                && entry.getSourcePermanentId() == null ? Subjects.target() : subject;
        List<Permanent> recipients = subjectResolutionService.permanents(gameData, entry, effect, recipientsSubject);

        Map<Permanent, Set<Keyword>> grants = new LinkedHashMap<>();
        for (Permanent target : recipients) {
            if (sourceLinked(grant.duration())
                    && (entry.getSourcePermanentId() == null
                    || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null)) {
                continue;
            }

            // Optional grant condition: the target stays legal either way; only the keyword grant
            // is conditional (e.g. Vampire's Zeal grants first strike only if the target is a Vampire).
            if (grant.grantCondition() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                    target, grant.grantCondition(), resolutionFilterContext(gameData, entry))) {
                continue;
            }

            // One-shot keyword grants to explicit permanents are floating layer-6 effects with
            // their own timestamp: a grant resolving after a "loses all abilities" or keyword
            // removal survives it, and vice versa (CR 613.7).
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, target, grant.keywords());
            if (grantableKeywords.isEmpty()) {
                continue;
            }
            grants.put(target, grantableKeywords);
        }
        for (Map.Entry<Permanent, Set<Keyword>> resolved : grants.entrySet()) {
            Permanent target = resolved.getKey();
            Set<Keyword> grantableKeywords = resolved.getValue();
            GrantKeywordEffect resolvedGrant = new GrantKeywordEffect(grantableKeywords, subject, grant.filter(),
                    grant.duration(), grant.grantCondition());
            UUID floatingSourceId = sourceLinked(grant.duration())
                    ? entry.getSourcePermanentId()
                    : null;
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), floatingSourceId, entry.getControllerId(), resolvedGrant,
                    target.getId(), null, null, grant.duration(), 0));
            String keywordNames = formatKeywords(grant.keywords());
            
            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" gains " + keywordNames + " " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keywords(), subject);
        }
    }

    /**
     * "Creatures you control gain …", "all creatures gain …": one floating grant per permanent the set names as the
     * effect resolves, so creatures arriving later are not affected (CR 611.2c).
     */
    private void resolveSet(GameData gameData, StackEntry entry, GrantKeywordEffect grant) {
        PermanentSubject subject = grant.subject();
        if (subject.declaresPlayerTarget()
                && subjectResolutionService.players(gameData, entry, grant, Subjects.targetPlayer()).isEmpty()) {
            return;
        }
        int count = 0;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, grant,
                Subjects.narrowed(subject, grant.filter()))) {
            // A layer-6 floating effect, so a grant resolving after an "all creatures you control lose all
            // abilities" on the same entry survives it (CR 613.7 timestamp order) — Dragonshift overloaded.
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
            if (grantableKeywords.isEmpty()) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, Subjects.target(), grant.filter(), grant.duration(),
                            grant.grantCondition()),
                    permanent.getId(), null, null, grant.duration(), 0));
            count++;
        }

        String noun = setNoun(subject);
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives "
                + formatKeywords(grant.keywords()) + " to " + count + " " + noun + " "
                + durationLabel(grant.duration()) + ".").build());
        log.info("Game {} - {} grants {} to {} {}", gameData.id, entry.getCard().getName(), grant.keywords(), count,
                noun);
    }

    /** What a set names, for the log: lands, any permanents, or creatures. */
    private static String setNoun(PermanentSubject set) {
        PermanentPredicate filter = set instanceof PermanentSubject.ControlledBy controlledBy
                ? controlledBy.filter() : null;
        if (filter instanceof PermanentTruePredicate
                || filter instanceof PermanentNotPredicate not
                && not.predicate() instanceof PermanentIsSourcePermanentPredicate) {
            return "permanent(s)";
        }
        if (filter instanceof PermanentIsLandPredicate
                || filter instanceof PermanentAllOfPredicate allOf
                && allOf.predicates().stream().anyMatch(PermanentIsLandPredicate.class::isInstance)) {
            return "land(s)";
        }
        return "creature(s)";
    }

    private FilterContext resolutionFilterContext(GameData gameData, StackEntry entry) {
        return FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withXValue(entry.getXValue());
    }

    private void resolveTargetAndSharingCreatures(GameData gameData, StackEntry entry,
                                                   GrantKeywordEffect grant) {
        List<Permanent> affectedPermanents = subjectResolutionService.permanents(gameData, entry, grant,
                grant.subject());

        int count = 0;
        for (Permanent permanent : affectedPermanents) {
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
            if (grantableKeywords.isEmpty()) {
                continue;
            }

            UUID floatingSourceId = sourceLinked(grant.duration())
                    ? entry.getSourcePermanentId()
                    : null;
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), floatingSourceId, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, Subjects.target(), null,
                            grant.duration(), grant.grantCondition()),
                    permanent.getId(), null, null, grant.duration(), 0));
            count++;
        }

        String keywordNames = formatKeywords(grant.keywords());
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" gives " + keywordNames + " to " + count + " creature(s) "
                        + durationLabel(grant.duration()) + ".").build());
        log.info("Game {} - {} grants {} to {} color-sharing creature(s)", gameData.id,
                entry.getCard().getName(), grant.keywords(), count);
    }

    void grantToPermanent(GameData gameData, StackEntry entry, Permanent permanent, Set<Keyword> keywords) {
        grantToPermanent(gameData, FaceDownVisibility.sourceName(gameData, entry), entry.getControllerId(), permanent, keywords);
    }

    void grantToPermanent(GameData gameData, String sourceName, UUID controllerId,
                          Permanent permanent, Set<Keyword> keywords) {
        Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, keywords);
        if (grantableKeywords.isEmpty()) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                sourceName, null, controllerId,
                new GrantKeywordEffect(grantableKeywords, Subjects.target()),
                permanent.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        String keywordNames = formatKeywords(grantableKeywords);
        gameLogService.append(gameData, GameLog.builder().card(permanent.getCard())
                .text(" gains " + keywordNames + " until end of turn.").build());
        log.info("Game {} - {} gains {} on return", gameData.id, permanent.getCard().getName(), grantableKeywords);
    }

    private boolean sourceLinked(Duration duration) {
        return Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration)
                || Duration.WHILE_SOURCE_REMAINS.equals(duration);
    }

    private Set<Keyword> grantableKeywords(GameData gameData, Permanent permanent, Set<Keyword> keywords) {
        return keywords.stream()
                .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword))
                .collect(java.util.stream.Collectors.toSet());
    }

    private String durationLabel(Duration duration) {
        if (Duration.UNTIL_END_OF_COMBAT.equals(duration)) {
            return "until end of combat";
        } else if (Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            return "until your next turn";
        } else if (Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP.equals(duration)) {
            return "until your next upkeep";
        } else if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration)) {
            return "for as long as you control its source";
        } else if (Duration.WHILE_SOURCE_REMAINS.equals(duration)) {
            return "for as long as its source remains on the battlefield";
        } else if (Duration.PERMANENT.equals(duration)) {
            return "indefinitely";
        }
        return "until end of turn";
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(k -> k.name().charAt(0) + k.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    /** Grants {@code grant}'s keywords to each of {@code permanentIds} still on the battlefield that its filter admits. */
    private void grantToEach(GameData gameData, StackEntry entry, GrantKeywordEffect grant, List<UUID> permanentIds,
                             String recipients) {
        FilterContext filterContext = resolutionFilterContext(gameData, entry);
        int count = 0;
        for (UUID targetId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null
                    || (grant.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(target, grant.filter(), filterContext))) {
                continue;
            }
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, target, grant.keywords());
            if (grantableKeywords.isEmpty()) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, Subjects.target(), grant.filter(),
                            grant.duration(), grant.grantCondition()),
                    target.getId(), null, null, grant.duration(), 0));
            count++;
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" gives " + formatKeywords(grant.keywords()) + " to " + count
                        + " " + recipients + " " + durationLabel(grant.duration()) + ".").build());
        log.info("Game {} - {} grants {} to {} {}", gameData.id, entry.getCard().getName(),
                grant.keywords(), count, recipients);
    }

}
