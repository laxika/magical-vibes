package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "13")
@CardRegistration(set = "ONC", collectorNumber = "51")
public class GoldwardensGambit extends Card {

    public GoldwardensGambit() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER)));

        CreateTokenEffect rebels = new CreateTokenEffect(
                CardType.CREATURE, 5, "Rebel", 2, 2, CardColor.RED, null,
                List.of(CardSubtype.REBEL), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of(Keyword.HASTE));
        addEffect(EffectSlot.SPELL, new CreateTokensAndAttachEquipmentEffect(rebels));
    }
}
