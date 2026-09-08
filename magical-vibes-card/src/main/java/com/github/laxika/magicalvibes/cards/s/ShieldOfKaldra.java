package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "139")
public class ShieldOfKaldra extends Card {

    private static final PermanentAllOfPredicate KALDRA_EQUIPMENT = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentNamedPredicate("Sword of Kaldra"),
                    new PermanentNamedPredicate("Shield of Kaldra"),
                    new PermanentNamedPredicate("Helm of Kaldra")))));

    public ShieldOfKaldra() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_PERMANENTS, KALDRA_EQUIPMENT));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF, KALDRA_EQUIPMENT));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
