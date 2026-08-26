package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "209")
public class IzoniCenterOfTheWeb extends Card {

    public IzoniCenterOfTheWeb() {
        CreateTokenEffect spiderTokens = new CreateTokenEffect(
                2, "Spider", 2, 1, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.SPIDER),
                Set.of(Keyword.REACH, Keyword.MENACE), Set.of());
        CardEffect collectEvidenceAndCreateSpiders = new MayEffect(
                SequenceEffect.of(
                        new CollectEvidenceEffect(4),
                        new ConditionalEffect(new EventValueAtLeast(4), spiderTokens)),
                "Collect evidence 4?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, collectEvidenceAndCreateSpiders);
        addEffect(EffectSlot.ON_ATTACK, collectEvidenceAndCreateSpiders);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(4, new PermanentIsTokenPredicate()),
                        new SurveilEffect(2),
                        new DrawCardEffect(2),
                        new GainLifeEffect(2)
                ),
                "Sacrifice four tokens: Surveil 2, then draw two cards. You gain 2 life."
        ));
    }
}
