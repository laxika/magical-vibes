package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastRevealedSpellWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealedFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cast one of the just-revealed instant or sorcery cards for free (Talent of the Telepath).
 *
 * <p>Accepting drops the sibling offers, removes the chosen card from the held group and — if the
 * group still allows another cast — re-offers the remaining held instants/sorceries before the
 * chosen spell goes on the stack. Declining leaves the other offers standing; once the last one is
 * declined the still-held cards go into the revealing player's graveyard.
 */
@Component
@RequiredArgsConstructor
public class MayCastRevealedSpellWithoutPayingHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final RevealedFreeCastSupport revealedFreeCastSupport;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastRevealedSpellWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PendingInteraction.RevealedFreeCastGroup group =
                gameData.pollPendingInteraction(PendingInteraction.RevealedFreeCastGroup.class);
        if (group == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card card = ability.sourceCard();
        List<Card> held = new ArrayList<>(group.heldCards());

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to cast ", card, "."));
            if (revealedFreeCastSupport.hasPendingOffers(gameData)) {
                gameData.queueInteraction(new PendingInteraction.RevealedFreeCastGroup(
                        group.ownerId(), group.casterId(), held, group.castsRemaining()));
            } else {
                revealedFreeCastSupport.dumpToGraveyard(gameData, group.ownerId(), held);
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        revealedFreeCastSupport.clearPendingOffers(gameData);
        held.removeIf(c -> c.getId().equals(card.getId()));
        revealedFreeCastSupport.offerOrDump(gameData, group.ownerId(), group.casterId(), held,
                group.castsRemaining() - 1);
        mayCastHandlerService.castRevealedCardWithoutPaying(gameData, player, card, group.ownerId());
    }
}
