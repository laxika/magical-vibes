package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEqualToCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "8")
@CardRegistration(set = "LTC", collectorNumber = "91")
public class SarumanTheWhiteHand extends Card {

    public SarumanTheWhiteHand() {
        // Whenever you cast a noncreature spell, amass Orcs X, where X is that spell's mana value.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new AmassGoblinsEqualToCastSpellManaValueEffect(CardSubtype.ORC))
        ));

        // Goblins and Orcs you control have ward {2}.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.WARD,
                GrantScope.OWN_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN)),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ORC))
                ))
        ));
    }
}
