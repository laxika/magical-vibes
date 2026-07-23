package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may draw a card unless that player pays {M}." Opponent chooses pay first; if they don't
 * (or can't), the source controller is offered an optional draw. Mystic Remora / Rhystic Study.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrawCardUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardUnlessPaysEffect) effect;

        UUID castingPlayerId = entry.getTargetId();
        UUID drawingPlayerId = entry.getControllerId();
        ManaCost cost = new ManaCost("{" + e.payAmount() + "}");
        ManaPool pool = gameData.playerManaPools.get(castingPlayerId);

        if (!cost.canPay(pool)) {
            // Can't pay — skip straight to the controller's optional draw.
            offerOptionalDraw(gameData, entry, e, drawingPlayerId);
            return;
        }

        // Can pay — ask the casting opponent. targetCardId carries the drawing player for the
        // decline path; manaCost marks this as the pay phase (vs the later draw-confirm phase).
        String prompt = "Pay {" + e.payAmount() + "}? If you don't, "
                + gameData.playerIdToName.get(drawingPlayerId) + " may draw "
                + (e.drawCount() == 1 ? "a card" : e.drawCount() + " cards")
                + ". (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), castingPlayerId, List.of(e), prompt,
                drawingPlayerId, "{" + e.payAmount() + "}", entry.getSourcePermanentId()
        ));
    }

    public static void offerOptionalDraw(GameData gameData, StackEntry entry, DrawCardUnlessPaysEffect effect,
                                         UUID drawingPlayerId) {
        offerOptionalDraw(gameData, entry.getCard(), effect, drawingPlayerId, entry.getSourcePermanentId());
    }

    public static void offerOptionalDraw(GameData gameData, com.github.laxika.magicalvibes.model.Card sourceCard,
                                         DrawCardUnlessPaysEffect effect, UUID drawingPlayerId,
                                         UUID sourcePermanentId) {
        String prompt = (effect.drawCount() == 1 ? "Draw a card?" : "Draw " + effect.drawCount() + " cards?")
                + " (" + sourceCard.getName() + ")";
        // manaCost null = draw-confirm phase (handled by the same mayfx bean).
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, drawingPlayerId, List.of(effect), prompt,
                null, null, sourcePermanentId
        ));
    }
}
