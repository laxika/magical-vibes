package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "98")
@CardRegistration(set = "ME2", collectorNumber = "172")
public class NaturesWrath extends Card {

    public NaturesWrath() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {G}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(new PayManaCost("{G}"), List.of(new SacrificeSelfEffect()), true));

        // Whenever a player puts an Island or blue permanent onto the battlefield, that player
        // sacrifices an Island or blue permanent of their choice. Same for Swamp/black permanents.
        // A basic Island is colorless (CR 202.2), so both halves of each clause are needed.
        addTrigger(Triggers.enters(landTypeOrColor(CardSubtype.ISLAND, CardColor.BLUE), ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.ISLAND, CardColor.BLUE));
        addTrigger(Triggers.enters(landTypeOrColor(CardSubtype.SWAMP, CardColor.BLACK), ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.SWAMP, CardColor.BLACK));
    }

    private static PermanentAnyOfPredicate landTypeOrColor(CardSubtype landType, CardColor color) {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(landType),
                new PermanentColorInPredicate(Set.of(color))));
    }

    private static CardEffect sacrificeTrigger(CardSubtype landType, CardColor color) {
        return new SacrificePermanentsEffect(1, landTypeOrColor(landType, color), SacrificeRecipient.TARGET_PLAYER);
    }
}
