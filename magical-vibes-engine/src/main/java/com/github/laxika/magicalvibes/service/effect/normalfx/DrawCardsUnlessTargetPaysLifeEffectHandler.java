package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.List;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsUnlessTargetPaysLifeEffect;
import org.springframework.stereotype.Component;

/** Resolves a targeted opponent's choice to pay life instead of letting the controller draw. */
@Component
public class DrawCardsUnlessTargetPaysLifeEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardsUnlessTargetPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardsUnlessTargetPaysLifeEffect drawEffect = (DrawCardsUnlessTargetPaysLifeEffect) effect;
        // "You draw N cards unless target opponent pays L life" (CR 118.12a), asked as the next step.
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                UnlessPaysEffect.unlessPaysLife(Subjects.targetPlayerDeclaredElsewhere(), drawEffect.lifeCost(),
                                new DrawCardEffect(drawEffect.drawCount()))
                        .withPrompt("Pay " + drawEffect.lifeCost() + " life to prevent drawing?")));
    }
}
