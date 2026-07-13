package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "258")
public class TranquilGrove extends Card {

    public TranquilGrove() {
        // {1}{G}{G}: Destroy all other enchantments.
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}{G}",
                List.of(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))))),
                "{1}{G}{G}: Destroy all other enchantments."));
    }
}
