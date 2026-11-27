package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PayFreeCastCostIncreaseEffect;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The cost-increase payment of a spell cast "without paying its mana cost" (CR 118.9) during a resolution: its total
 * cost is its cost increases (CR 601.2f). The caster's mana pool pays them at once when it can. Otherwise, when the
 * mana the caster's untapped sources could make covers them, the cast pauses on a "you may" question with that mana
 * cost, during which the caster may activate mana abilities (CR 601.2g, 608.2g; the same window as every other
 * mid-resolution mana payment, {@code GameService.isMayCostManaPayment}); its answer is
 * {@link PayFreeCastCostIncreaseEffect}'s handler, which resumes or abandons the paused flow. When even that mana
 * can't cover them, the spell can't be cast.
 */
@Component
public class FreeCastCostIncreaseSupport {

    /** What became of the payment. */
    public enum Payment {
        /** Paid from the pool (or there was nothing to pay): the cast goes on. */
        PAID,
        /** The cast paused on the payment question; the flow returns and its answer resumes it. */
        ASKED,
        /** Not payable even with mana abilities: the spell can't be cast. */
        UNPAYABLE
    }

    private final CastingCostService castingCostService;
    private final PotentialManaService potentialManaService;
    @Autowired
    @Lazy
    private SpellCastingService spellCastingService;
    @Autowired
    @Lazy
    private PlayerInputService playerInputService;

    public FreeCastCostIncreaseSupport(CastingCostService castingCostService,
                                       PotentialManaService potentialManaService) {
        this.castingCostService = castingCostService;
        this.potentialManaService = potentialManaService;
    }

    /**
     * Pays {@code card}'s cost increases from the pool, or pauses the cast on the payment question.
     *
     * @param cardId the id the paused flow resumes by (the exile id for an exile cast)
     * @param paused the paused flow and what it needs to go on
     */
    public Payment payOrAsk(GameData gameData, UUID casterId, Card card, UUID cardId, Zone zone,
                            PayFreeCastCostIncreaseEffect.PausedCast paused) {
        if (spellCastingService.payFreeCastCostIncreases(gameData, casterId, card, zone)) {
            return Payment.PAID;
        }
        if (!payableWithManaAbilities(gameData, casterId, card, zone)) {
            return Payment.UNPAYABLE;
        }
        int increase = castingCostService.getCastCostModifier(gameData, casterId, card, 0, zone);
        String cost = "{" + increase + "}";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(card, casterId,
                List.of(new PayFreeCastCostIncreaseEffect(cardId, zone, paused)),
                "Pay " + cost + " to cast " + card.getName() + " (its cost increase)?", cardId, cost));
        if (!gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
        return Payment.ASKED;
    }

    /**
     * Whether the pool, or the pool and the mana the caster's untapped sources could make, covers the spell's cost
     * increases (nothing to pay counts as covered).
     */
    public boolean payableWithManaAbilities(GameData gameData, UUID casterId, Card card, Zone zone) {
        if (spellCastingService.canPayFreeCastCostIncreases(gameData, casterId, card, zone)) {
            return true;
        }
        int increase = castingCostService.getCastCostModifier(gameData, casterId, card, 0, zone);
        return new ManaCost("{0}").canPayWithAdditionalGenericCost(
                potentialManaService.buildVirtualManaPool(gameData, casterId), 0, increase);
    }

    /**
     * Whether the caster can pay {@code cost}, an optional mana cost offered during a free cast (a kicker), from the
     * pool or with the mana their untapped sources could make while the offer is asked (CR 601.2g).
     */
    public boolean manaPayableWithManaAbilities(GameData gameData, UUID casterId, ManaCost cost) {
        ManaPool pool = gameData.playerManaPools.get(casterId);
        return pool != null && cost.canPay(pool, 0)
                || cost.canPay(potentialManaService.buildVirtualManaPool(gameData, casterId), 0);
    }
}
