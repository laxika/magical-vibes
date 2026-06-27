package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherSubtypePermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherSubtypePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherSubtypePermanentEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        UUID originalTargetId = e.originalTargetId();
        CardSubtype subtype = e.subtype();
        Card spellCard = spellSnapshot.getCard();

        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((pid, perm) -> {
            if (perm.getId().equals(originalTargetId)) return;
            if (!perm.getCard().getSubtypes().contains(subtype)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId)) return;

            eligibleTargets.add(perm);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId());

            gameData.stack.add(copyEntry);

            String logMsg = "A copy of " + spellCard.getName() + " is created targeting " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other {}",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(),
                spellCard.getName(), subtype.getDisplayName());
    }
}
