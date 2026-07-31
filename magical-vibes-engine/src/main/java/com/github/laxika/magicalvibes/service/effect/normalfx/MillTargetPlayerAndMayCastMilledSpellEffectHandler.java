package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastMilledSpellWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndMayCastMilledSpellEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Target player mills N cards. You may cast an instant or sorcery spell from among them without
 * paying its mana cost." (Jace's Mindseeker.)
 *
 * <p>Only the cards milled by this resolution are eligible, so the milled list is captured and each
 * instant or sorcery still sitting in a graveyard is offered as its own {@link PendingMayAbility};
 * the may handler casts the accepted one and clears the remaining offers.
 */
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndMayCastMilledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndMayCastMilledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillTargetPlayerAndMayCastMilledSpellEffect e = (MillTargetPlayerAndMayCastMilledSpellEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerDecks.containsKey(targetPlayerId)) return;

        List<Card> milled = graveyardService.resolveMillPlayer(gameData, targetPlayerId, e.count());

        List<Card> castable = milled.stream()
                .filter(c -> c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY))
                .filter(c -> gameQueryService.findCardInGraveyardById(gameData, c.getId()) != null)
                .toList();

        for (int i = castable.size() - 1; i >= 0; i--) {
            Card card = castable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card, entry.getControllerId(),
                    List.of(new MayCastMilledSpellWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
