package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (target == null || targetControllerId == null) {
            return;
        }

        String targetControllerName = gameData.playerIdToName.get(targetControllerId);
        if (!permanentRemovalService.removePermanentToGraveyard(gameData, target)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(targetControllerName + " sacrifices ", target.getCard(), "."));

        CardAllOfPredicate noncreatureArtifact = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        SearchLibraryEffect search = new SearchLibraryEffect(
                noncreatureArtifact, LibrarySearchDestination.BATTLEFIELD);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetControllerId,
                List.of(search),
                entry.getCard().getName() + " — Search your library for a noncreature artifact card?",
                null,
                null,
                entry.getSourcePermanentId()));

        log.info("Game {} - {} sacrifices {} and may search their library for a noncreature artifact",
                gameData.id, targetControllerName, target.getCard().getName());
    }
}
