package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MAR", collectorNumber = "31")
public class Arachnogenesis extends Card {

    public Arachnogenesis() {
        // Create one 1/2 green Spider creature token with reach for each creature attacking you.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsAttackingSourceControllerPredicate(), CountScope.ANY_PLAYER),
                "Spider", 1, 2, CardColor.GREEN, List.of(CardSubtype.SPIDER),
                Set.of(Keyword.REACH), Set.of()));

        // Prevent all combat damage that would be dealt this turn by non-Spider creatures.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));
    }
}
