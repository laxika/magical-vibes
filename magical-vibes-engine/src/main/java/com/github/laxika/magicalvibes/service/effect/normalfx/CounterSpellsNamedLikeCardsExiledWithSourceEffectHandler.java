package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellsNamedLikeCardsExiledWithSourceEffect;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Grimoire Thief's sacrifice ability: turns all cards exiled with the source face up and counters
 * every spell on the stack whose name matches one of those exiled cards. The exiled cards are looked
 * up by the resolving entry's {@code sourcePermanentId} (set even though the source was sacrificed as
 * a cost) and stay in exile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSpellsNamedLikeCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL);

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellsNamedLikeCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        Set<String> exiledNames = new HashSet<>();
        for (Card card : gameData.getCardsExiledByPermanent(sourcePermanentId)) {
            exiledNames.add(card.getName());
        }
        if (exiledNames.isEmpty()) return;

        // Snapshot matching spells first — countering mutates the stack.
        List<StackEntry> toCounter = new ArrayList<>();
        for (StackEntry se : gameData.stack) {
            if (se == entry) continue;
            if (SPELL_TYPES.contains(se.getEntryType()) && exiledNames.contains(se.getCard().getName())) {
                toCounter.add(se);
            }
        }

        for (StackEntry target : toCounter) {
            StackEntry resolved = counterSupport.findCounterTarget(gameData, target.getCard().getId(), entry);
            if (resolved != null) {
                counterSupport.counterSpell(gameData, entry, resolved);
            }
        }
    }
}
