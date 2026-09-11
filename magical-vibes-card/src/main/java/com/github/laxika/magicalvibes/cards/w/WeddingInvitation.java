package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "260")
public class WeddingInvitation extends Card {

    public WeddingInvitation() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        PermanentHasSubtypePredicate vampire = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new MakeCreatureUnblockableEffect(),
                        GrantKeywordEffect.toTargetIf(Keyword.LIFELINK, vampire)
                ),
                "{T}, Sacrifice this artifact: Target creature can't be blocked this turn. If it's a Vampire, it also gains lifelink until end of turn.",
                TargetFilters.creature()
        ));
    }
}
