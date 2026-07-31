package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "98")
public class NaturesWrath extends Card {

    public NaturesWrath() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {G}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(new PayManaCost("{G}"), List.of(new SacrificeSelfEffect()), true));

        // Whenever a player puts an Island or blue permanent onto the battlefield, that player
        // sacrifices an Island or blue permanent of their choice. Same for Swamp/black permanents.
        // A basic Island is colorless (CR 202.2), so both halves of each clause are needed.
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                sacrificeTrigger(CardSubtype.ISLAND, CardColor.BLUE));
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                sacrificeTrigger(CardSubtype.SWAMP, CardColor.BLACK));
    }

    private static CardEffect sacrificeTrigger(CardSubtype landType, CardColor color) {
        return new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(landType),
                        new CardColorPredicate(color))),
                new SacrificePermanentsEffect(
                        1,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(landType),
                                new PermanentColorInPredicate(Set.of(color)))),
                        SacrificeRecipient.TARGET_PLAYER));
    }
}
