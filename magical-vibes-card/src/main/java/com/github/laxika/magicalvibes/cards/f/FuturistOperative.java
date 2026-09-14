package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "53")
public class FuturistOperative extends Card {

    public FuturistOperative() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsTapped(),
                new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.SELF, true)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsTapped(),
                new GrantSubtypeEffect(CardSubtype.CITIZEN, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsTapped(),
                new SetBasePowerToughnessEffect(1, 1, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsTapped(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(false, "{2}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{2}{U}: Untap this creature."));
    }
}
