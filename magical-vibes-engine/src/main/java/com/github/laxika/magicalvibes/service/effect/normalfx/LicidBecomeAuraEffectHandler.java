package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.effect.LicidEndEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LicidBecomeAuraEffect}: the source stops being a creature, becomes an Aura with
 * enchant creature whose only activated ability is the "end this effect" payment, and attaches to
 * the targeted creature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicidBecomeAuraEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LicidBecomeAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Permanent host = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (host == null || !gameQueryService.isCreature(gameData, host)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (no legal creature to attach to)."));
            log.info("Game {} - {} licid ability fizzles (illegal target)", gameData.id, entry.getCard().getName());
            return;
        }

        source.setCard(auraForm(source.getCard(), ((LicidBecomeAuraEffect) effect).endCost()));
        gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        source.setAttachedTo(host.getId());
        source.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" becomes an Aura attached to ")
                .card(host.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} becomes an Aura attached to {}",
                gameData.id, source.getCard().getName(), host.getCard().getName());
    }

    /**
     * Builds the Aura face as a runtime copy — live cards are frozen, so the printed face cannot be
     * mutated. The Licid ability is dropped ("loses this ability") and replaced by the end-effect
     * payment; the STATIC effects come along untouched.
     */
    private Card auraForm(Card creatureForm, String endCost) {
        Card copy = creatureForm.createRuntimeCopy();
        copy.setType(CardType.ENCHANTMENT);
        copy.setSubtypes(List.of(CardSubtype.AURA));
        copy.setPower(null);
        copy.setToughness(null);
        copy.getActivatedAbilities().clear();
        copy.addActivatedAbility(new ActivatedAbility(
                false,
                endCost,
                List.of(new LicidEndEffect()),
                endCost + ": End this effect."
        ));
        copy.target(TargetFilters.creature());
        copy.freeze();
        return copy;
    }
}
