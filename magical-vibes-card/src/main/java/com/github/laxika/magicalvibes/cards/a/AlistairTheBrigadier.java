package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "112")
@CardRegistration(set = "WHO", collectorNumber = "401")
@CardRegistration(set = "WHO", collectorNumber = "717")
@CardRegistration(set = "WHO", collectorNumber = "992")
public class AlistairTheBrigadier extends Card {

    public AlistairTheBrigadier() {
        // Whenever you cast a historic spell, create a 1/1 white Soldier creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of()))));

        // Whenever Alistair attacks, you may pay {8}. If you do, creatures you control get +X/+X
        // until end of turn, where X is the number of historic permanents you control.
        PermanentCount historicPermanents = new PermanentCount(
                new PermanentIsHistoricPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{8}",
                new BoostAllOwnCreaturesEffect(historicPermanents, historicPermanents),
                "Pay {8} to boost creatures you control?"));
    }
}
