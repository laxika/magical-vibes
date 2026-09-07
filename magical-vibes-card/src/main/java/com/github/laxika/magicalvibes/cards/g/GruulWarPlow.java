package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "151")
public class GruulWarPlow extends Card {

    public GruulWarPlow() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES, new PermanentIsCreaturePredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{G}",
                List.of(new AnimatePermanentsEffect(
                        4, 4, List.of(CardSubtype.JUGGERNAUT), Set.of(), null, Set.of(CardType.ARTIFACT))),
                "{1}{R}{G}: This artifact becomes a 4/4 Juggernaut artifact creature until end of turn."
        ));
    }
}
