package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSorceryCasterForBackdraftEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseSorceryCasterForBackdraftEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseSorceryCasterForBackdraftEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var eligible = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getSpellsCastThisTurn(playerId).stream()
                        .anyMatch(card -> card.hasType(CardType.SORCERY)))
                .toList();
        if (eligible.isEmpty()) {
            return;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BackdraftPlayerChoice());
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), eligible,
                "Backdraft — Choose a player who cast a sorcery this turn.");
    }
}
