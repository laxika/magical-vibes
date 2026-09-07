package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "129")
public class CrescentIslandTemple extends Card {

    public CrescentIslandTemple() {
        PermanentCount shrinesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, monkToken(CardType.CREATURE, shrinesYouControl));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SHRINE), monkToken()));
    }

    private static CreateTokenEffect monkToken() {
        return new CreateTokenEffect(1, "Monk", 1, 1, CardColor.RED, List.of(CardSubtype.MONK),
                Set.of(), Set.of(), prowessEffect());
    }

    private static CreateTokenEffect monkToken(CardType primaryType, PermanentCount amount) {
        return new CreateTokenEffect(primaryType, amount, "Monk", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.MONK), Set.of(), Set.of(), false, false, prowessEffect(), List.of(),
                false, false, false, 0, Set.of());
    }

    private static Map<EffectSlot, CardEffect> prowessEffect() {
        return Map.of(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
    }
}
