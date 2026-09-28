package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureWithManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a per-creature mana-paid spell-copy choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherCreatureWithManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherCreatureWithManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopySpellForEachOtherCreatureWithManaEffect) effect;
        if (copyEffect.spellSnapshot() == null) {
            return;
        }

        StackEntry spellSnapshot = copyEffect.spellSnapshot();
        Card spellCard = spellSnapshot.getCard();
        if (spellCard.isCantBeCopied()) {
            return;
        }

        List<Permanent> eligibleTargets = eligibleTargets(gameData, spellCard,
                copyEffect.castingPlayerId(), copyEffect.originalTargetId());
        if (eligibleTargets.isEmpty()) {
            return;
        }

        int maxCount = Math.min(eligibleTargets.size(), affordableCount(
                gameData.playerManaPools.get(entry.getControllerId()),
                new ManaCost(copyEffect.manaCost()), eligibleTargets.size()));
        if (maxCount == 0) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                eligibleTargets.stream().map(Permanent::getId).toList(),
                maxCount,
                new MultiPermanentChoiceContext.CopySpellForEachOtherCreatureWithMana(copyEffect),
                "Choose any number of other creatures to copy the spell onto (pay "
                        + copyEffect.manaCost() + " for each).");
    }

    public void completeChoice(GameData gameData, List<UUID> selectedIds,
                               MultiPermanentChoiceContext.CopySpellForEachOtherCreatureWithMana context) {
        StackEntry triggerEntry = gameData.pendingEffectResolutionEntry;
        if (triggerEntry == null) {
            return;
        }

        CopySpellForEachOtherCreatureWithManaEffect copyEffect = context.effect();
        StackEntry spellSnapshot = copyEffect.spellSnapshot();
        if (spellSnapshot == null || spellSnapshot.getCard().isCantBeCopied()) {
            return;
        }

        List<Permanent> eligibleTargets = eligibleTargets(gameData, spellSnapshot.getCard(),
                copyEffect.castingPlayerId(), copyEffect.originalTargetId());
        List<Permanent> chosenTargets = selectedIds.stream()
                .map(id -> eligibleTargets.stream()
                        .filter(permanent -> permanent.getId().equals(id))
                        .findFirst()
                        .orElse(null))
                .filter(permanent -> permanent != null)
                .toList();
        ManaCost cost = new ManaCost(copyEffect.manaCost());
        ManaPool pool = gameData.playerManaPools.get(triggerEntry.getControllerId());
        ManaPool remaining = pool == null ? null : new ManaPool(pool);
        if (remaining == null) {
            return;
        }
        for (int i = 0; i < chosenTargets.size(); i++) {
            if (!cost.canPay(remaining)) {
                return;
            }
            cost.pay(remaining);
        }
        for (int i = 0; i < chosenTargets.size(); i++) {
            cost.pay(pool);
        }

        for (Permanent target : chosenTargets) {
            Card copyCard = isPermanentSpell(spellSnapshot.getEntryType())
                    ? copySupport.createTokenCopyCard(spellSnapshot.getCard())
                    : copySupport.createCopyCard(spellSnapshot.getCard());
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    spellSnapshot, copyCard, triggerEntry.getControllerId(), target.getId());
            for (int i = 0; i < copyEntry.getDeclaredTargetIds().size(); i++) {
                copyEntry.replaceTargetIdAt(i, target.getId());
            }
            copySupport.addCopyToStack(gameData, copyEntry);
            gameLogService.append(gameData, GameLog.builder()
                    .text("A copy of ").card(spellSnapshot.getCard())
                    .text(" is created targeting ").card(target.getCard()).text(".").build());
        }
    }

    private List<Permanent> eligibleTargets(GameData gameData, Card spellCard,
                                             UUID castingPlayerId, UUID originalTargetId) {
        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getId().equals(originalTargetId)
                    || !gameQueryService.isCreature(gameData, permanent)
                    || !validTargetService.canPermanentBeTargetedBySpell(
                    gameData, permanent, spellCard, castingPlayerId)) {
                return;
            }
            eligibleTargets.add(permanent);
        });
        return eligibleTargets;
    }

    private int affordableCount(ManaPool pool, ManaCost perCreatureCost, int candidateCount) {
        if (pool == null) {
            return 0;
        }
        ManaPool remaining = new ManaPool(pool);
        int affordable = 0;
        while (affordable < candidateCount && perCreatureCost.canPay(remaining)) {
            perCreatureCost.pay(remaining);
            affordable++;
        }
        return affordable;
    }

    private boolean isPermanentSpell(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case INSTANT_SPELL, SORCERY_SPELL, TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
