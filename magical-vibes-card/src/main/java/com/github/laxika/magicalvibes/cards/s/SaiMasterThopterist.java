package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "69")
public class SaiMasterThopterist extends Card {

    public SaiMasterThopterist() {
        // Whenever you cast an artifact spell, create a 1/1 colorless Thopter artifact creature token with flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new CreateTokenEffect("Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT)))));

        // {1}{U}, Sacrifice two artifacts: Draw a card.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(
                new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                new DrawCardEffect(1)),
                "{1}{U}, Sacrifice two artifacts: Draw a card."));
    }
}
