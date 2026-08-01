package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Populate (CR 701.36a) — "choose a creature token you control, then create a token that's a copy of
 * that creature token."
 *
 * <p>Not a targeted choice: it is made as the spell or ability resolves. No creature token means no
 * token is created (CR 701.36b); exactly one means the choice is forced and made here; otherwise the
 * controller is prompted via {@link PermanentChoiceContext.Populate}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopulateEffectHandler implements NormalEffectHandlerBean {

    private final PopulateSupport populateSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PopulateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> tokens = populateSupport.creatureTokensControlledBy(gameData, controllerId);

        if (tokens.isEmpty()) {
            log.info("Game {} - Populate creates nothing: controller has no creature tokens", gameData.id);
            return;
        }

        if (tokens.size() == 1) {
            populateSupport.createCopy(gameData, controllerId, tokens.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.Populate(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                new java.util.ArrayList<>(tokens.stream().map(Permanent::getId).toList()),
                "Populate: choose a creature token you control to copy.");
    }
}
