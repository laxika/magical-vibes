package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "43")
public class Riddleform extends Card {

    public Riddleform() {
        // Whenever you cast a noncreature spell, you may have this enchantment become a 3/3
        // Sphinx creature with flying in addition to its other types until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.SPHINX), Set.of(Keyword.FLYING)))),
                "Have Riddleform become a 3/3 Sphinx creature with flying?"));

        // {2}{U}: Scry 1.
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}", List.of(new ScryEffect(1)),
                "{2}{U}: Scry 1."));
    }
}
