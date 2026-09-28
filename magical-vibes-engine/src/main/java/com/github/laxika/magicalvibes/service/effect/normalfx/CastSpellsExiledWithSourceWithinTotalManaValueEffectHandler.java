package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsExiledWithSourceWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Offers the source's exiled spells for free, with the ability's X as a total mana-value cap. */
@Component
@RequiredArgsConstructor
public class CastSpellsExiledWithSourceWithinTotalManaValueEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastSpellsExiledWithSourceWithinTotalManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        List<UUID> castableSpellIds = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(CastSpellsExiledWithSourceWithinTotalManaValueEffectHandler::isSpell)
                .map(Card::getId)
                .toList();
        if (castableSpellIds.isEmpty()) return;

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        entry.getControllerId(), castableSpellIds, castableSpellIds.size(),
                        "You may cast any number of spells exiled with " + entry.getCard().getName()
                                + " without paying their mana costs.",
                        false, entry.getXValue()));
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
