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
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
        if (grant.scope() == GrantScope.OWN_CREATURES
                || grant.scope() == GrantScope.ALL_OWN_CREATURES) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(grant.scope().subject(), grant.filter()))) {
                // A layer-6 floating effect, so a grant resolving after an "all creatures you control
                // lose all abilities" on the same entry survives it (CR 613.7 timestamp order) —
                // Dragonshift overloaded.
                Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
                if (grantableKeywords.isEmpty()) {
                    continue;
                }
                gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, grant.filter(), grant.duration(), grant.grantCondition()),
                        permanent.getId(), null, null, grant.duration(), 0));
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());

            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.OWN_PERMANENTS) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(GrantScope.OWN_PERMANENTS.subject(), grant.filter()))) {
                Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
                if (grantableKeywords.isEmpty()) {
                    continue;
                }
                gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
                                grant.duration(), grant.grantCondition()),
                        permanent.getId(), null, null, grant.duration(), 0));
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " permanent(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} own permanent(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.TARGETS) {
            StackEntry triggeringSpell = entry.getTriggeringCardId() == null
                    ? null
                    : gameQueryService.findStackEntryByCardId(gameData, entry.getTriggeringCardId());
            List<UUID> targetIds;
            if (triggeringSpell == null) {
                targetIds = entry.getTriggeringSpellTargetIds().isEmpty()
                        ? entry.targetsForEffect(grant) : entry.getTriggeringSpellTargetIds();
            } else {
                targetIds = new ArrayList<>();
                if (triggeringSpell.getTargetId() != null) {
                    targetIds.add(triggeringSpell.getTargetId());
                }
                targetIds.addAll(triggeringSpell.getDeclaredTargetIds());
            }
            grantToEach(gameData, entry, grant, targetIds, "target creature(s)");
            return;
        }

        if (grant.scope() == GrantScope.DECLARED_ATTACKERS) {
            // "They gain …" of a "whenever you attack" ability: the attacking creatures the entry recorded as the
            // ability triggered, wherever they are now.
            grantToEach(gameData, entry, grant, entry.getAttackingPermanentSnapshots().stream()
                    .map(Permanent::getId).toList(), "attacking creature(s)");
            return;
        }

        if (grant.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            if (subjectResolutionService.players(gameData, entry, effect, Subjects.targetPlayer()).isEmpty()) {
                return;
            }
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(GrantScope.TARGET_PLAYERS_CREATURES.subject(), grant.filter()))) {
                Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
                if (grantableKeywords.isEmpty()) {
                    continue;
                }
                gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, grant.filter(),
                                grant.duration(), grant.grantCondition()),
                        permanent.getId(), null, null, grant.duration(), 0));
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} creature(s) target player controls", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.TARGET_AND_SHARING_CREATURES) {
            resolveTargetAndSharingCreatures(gameData, entry, grant);
            return;
        }

        if (grant.scope() == GrantScope.ENCHANTED_CREATURE
                || grant.scope() == GrantScope.ENCHANTED_PERMANENT
                || grant.scope() == GrantScope.EQUIPPED_CREATURE) {
            Permanent target = subjectResolutionService.permanent(gameData, entry, effect, grant.scope().subject());
            boolean creatureScope = grant.scope() == GrantScope.ENCHANTED_CREATURE
                    || grant.scope() == GrantScope.EQUIPPED_CREATURE;
            if (target == null || (creatureScope && !gameQueryService.isCreature(gameData, target))) {
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
                    entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
                            grant.duration(), grant.grantCondition()),
                    target.getId(), null, null, grant.duration(), 0));

            String keywordNames = formatKeywords(grant.keywords());
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" gains " + keywordNames + " " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(),
                    grant.keywords(), grant.scope());
            return;
        }

        if (grant.scope() == GrantScope.ALL_CREATURES
                || grant.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF) {
            // The one-shot "all creatures" includes the source.
            final int[] count = {0};
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(GrantScope.ALL_CREATURES_INCLUDING_SELF.subject(), grant.filter()))) {
                Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
                if (grantableKeywords.isEmpty()) {
                    continue;
                }
                gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
                                grant.duration(), grant.grantCondition()),
                        permanent.getId(), null, null, grant.duration(), 0));
                count[0]++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count[0] + " creature(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count[0]);
            return;
        }

        if (grant.scope() == GrantScope.ALL_LANDS) {
            // The one-shot "all lands" includes the source.
            final int[] count = {0};
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(GrantScope.ALL_LANDS_INCLUDING_SELF.subject(), grant.filter()))) {
                Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, grant.keywords());
                if (grantableKeywords.isEmpty()) {
                    continue;
                }
                // A layer-6 floating effect, so a grant resolving after an earlier "loses all
                // abilities" effect on the land survives it (CR 613.7 timestamp order).
                gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, grant.filter(), grant.duration(), grant.grantCondition()),
                        permanent.getId(), null, null, grant.duration(), 0));
                count[0]++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count[0] + " land(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} land(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count[0]);
            return;
        }

        // SELF resolves against the source; TARGET may cover multiple targets when the effect is
        // bound to a target group (e.g. Blades of Velis Vel: "up to two target creatures").
        List<Permanent> recipients;
        if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.TARGET
                || grant.scope() == GrantScope.TRIGGERING_PERMANENT) {
            // "It gains haste" after a spell's "put a card onto the battlefield, then": the put handler records the
            // permanent as the entry's target, and the spell has no source permanent (Terminal Velocity).
            PermanentSubject subject = grant.scope() == GrantScope.SELF && entry.getSourcePermanentId() == null
                    ? Subjects.target() : grant.scope().subject();
            recipients = subjectResolutionService.permanents(gameData, entry, effect, subject);
        } else if (grant.scope() == GrantScope.BANDED_WITH_SELF) {
            recipients = livePermanents(gameData, bandmatesOf(gameData, entry));
        } else if (grant.scope() == GrantScope.TOKENS_CREATED_THIS_RESOLUTION) {
            recipients = livePermanents(gameData, entry.getCreatedPermanentIds());
        } else {
            return;
        }

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
            GrantKeywordEffect resolvedGrant = new GrantKeywordEffect(grantableKeywords, grant.scope(), grant.filter(), grant.duration(), grant.grantCondition());
            UUID floatingSourceId = sourceLinked(grant.duration())
                    ? entry.getSourcePermanentId()
                    : null;
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    entry.getCard().getName(), floatingSourceId, entry.getControllerId(), resolvedGrant,
                    target.getId(), null, null, grant.duration(), 0));
            String keywordNames = formatKeywords(grant.keywords());
            
            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" gains " + keywordNames + " " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keywords(), grant.scope());
        }
    }

    /** The permanents of {@code ids} still on the battlefield, for the constants that have no subject yet. */
    private List<Permanent> livePermanents(GameData gameData, java.util.Collection<UUID> ids) {
        List<Permanent> live = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                live.add(permanent);
            }
        }
        return live;
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
        UUID targetId = entry.targetsForEffect(grant).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);
        List<Permanent> affectedPermanents = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (permanent.getId().equals(targetId)
                    || (!targetColors.isEmpty()
                    && gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .anyMatch(targetColors::contains)))) {
                affectedPermanents.add(permanent);
            }
        });

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
                    entry.getCard().getName(), floatingSourceId, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
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
        grantToPermanent(gameData, entry.getCard().getName(), entry.getControllerId(), permanent, keywords);
    }

    void grantToPermanent(GameData gameData, String sourceName, UUID controllerId,
                          Permanent permanent, Set<Keyword> keywords) {
        Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, keywords);
        if (grantableKeywords.isEmpty()) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                sourceName, null, controllerId,
                new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET),
                permanent.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        String keywordNames = formatKeywords(grantableKeywords);
        gameLogService.append(gameData, GameLog.builder().card(permanent.getCard())
                .text(" gains " + keywordNames + " until end of turn.").build());
        log.info("Game {} - {} gains {} on return", gameData.id, permanent.getCard().getName(), grantableKeywords);
    }

    /**
     * The other attacking creatures sharing the source's attacking band (CR 702.22c). The source
     * itself is excluded, and an empty list is returned when it is not attacking in a band.
     */
    private List<UUID> bandmatesOf(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return List.of();
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || !source.isAttacking() || source.getBandId() == null) {
            return List.of();
        }
        UUID bandId = source.getBandId();
        List<UUID> bandmates = new java.util.ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking()
                    && bandId.equals(permanent.getBandId())
                    && !permanent.getId().equals(sourceId)) {
                bandmates.add(permanent.getId());
            }
        });
        return bandmates;
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
                    entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, grant.filter(),
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
