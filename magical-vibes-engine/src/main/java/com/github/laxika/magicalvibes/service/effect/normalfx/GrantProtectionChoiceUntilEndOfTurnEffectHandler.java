package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantProtectionChoiceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionChoiceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantProtectionChoiceUntilEndOfTurnEffect) effect;

        // Multi-target spells (Prismatic Boon's "X target creatures") carry their targets in the
        // flat list; single-target spells and abilities carry theirs on targetId.
        List<Permanent> targets = subjectResolutionService.permanents(gameData, entry, effect, recipients(e));
        UUID choosingPlayerId = e.targetControllerChooses()
                && !targets.isEmpty()
                ? gameQueryService.findPermanentController(gameData, targets.getFirst().getId())
                : entry.getControllerId();
        if (choosingPlayerId == null) {
            choosingPlayerId = entry.getControllerId();
        }

        playerInputService.beginProtectionColorChoice(gameData, choosingPlayerId,
                targets.stream().map(Permanent::getId).toList(), e.includeArtifacts(), e.includeColorless());
    }

    /**
     * Self-scoped abilities ("this creature gains protection from the color of your choice") have no
     * target and resolve against the source permanent; the mass form scans the controller's own
     * creatures on resolution; targeted ones use the flat list (Prismatic Boon's "X target
     * creatures") or the single {@code targetId}.
     */
    private static PermanentSubject recipients(GrantProtectionChoiceUntilEndOfTurnEffect e) {
        return switch (e.scope()) {
            case SELF -> GrantScope.SELF.subject();
            // The one-shot "creatures you control" and "all creatures" include the source.
            case OWN_CREATURES -> Subjects.narrowed(GrantScope.ALL_OWN_CREATURES.subject(), e.filter());
            case ALL_CREATURES -> GrantScope.ALL_CREATURES_INCLUDING_SELF.subject();
            default -> Subjects.allTargets();
        };
    }
}
