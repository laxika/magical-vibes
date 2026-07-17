package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "This permanent deals N damage to the triggering player unless they pay {M}." When the player
 * can't pay the damage is applied immediately (through the normal {@link DealDamageToPlayersEffect}
 * path so prevention/redirection/infect apply); otherwise the choice is offered via the may-ability
 * system. Soul Barrier.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageUnlessPaysEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        ManaCost cost = new ManaCost("{" + e.payAmount() + "}");
        ManaPool pool = gameData.playerManaPools.get(targetPlayerId);

        if (!cost.canPay(pool)) {
            // Can't pay — apply the damage now, reusing the real entry (correct source + target).
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(e.damage(), DamageRecipient.TARGET_PLAYER);
            dealDamageToPlayersEffectHandler.resolve(gameData, entry, damage);
            return;
        }

        // Can pay — ask the target player via the may ability system.
        String prompt = "Pay {" + e.payAmount() + "}? If you don't, " + entry.getCard().getName()
                + " deals " + e.damage() + " damage to you.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt, null, null, entry.getSourcePermanentId()
        ));
    }
}
