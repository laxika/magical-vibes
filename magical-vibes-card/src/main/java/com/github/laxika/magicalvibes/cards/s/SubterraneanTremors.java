package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOA", collectorNumber = "48")
public class SubterraneanTremors extends Card {

    public SubterraneanTremors() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new XValue(), false, false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(4),
                new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(8),
                new CreateTokenEffect("Lizard", 8, 8, CardColor.RED,
                        List.of(CardSubtype.LIZARD), Set.of(), Set.of())));
    }
}
