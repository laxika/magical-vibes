package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeForetoldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link BecomeForetoldEffect}. It never prompts. */
@Component
@RequiredArgsConstructor
public class BecomeForetoldEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeForetoldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var foretold = (BecomeForetoldEffect) effect;
        for (var located : subjectResolutionService.locatedCards(gameData, entry, foretold, foretold.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            Card card = located.card();
            ManaCost cost = card.getParsedManaCost() == null
                    ? null : card.getParsedManaCost().reducedBy(new ManaCost("{" + foretold.reducedBy() + "}"));
            if (gameData.markExiledCardAsForetold(card.getId(), entry.getControllerId(), cost)) {
                // The card is face down, so it isn't named (CR 406.3).
                gameLogService.append(gameData, GameLog.text("The exiled card becomes foretold with its mana cost "
                        + "reduced by {" + foretold.reducedBy() + "}."));
            }
        }
    }
}
