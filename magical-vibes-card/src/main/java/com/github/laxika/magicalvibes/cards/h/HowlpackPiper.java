package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WildsongHowler;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "205")
public class HowlpackPiper extends Card {

    public HowlpackPiper() {
        setBackFaceCard(new WildsongHowler());

        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature")
                                .untapSourceIfEnteredCardHasAnySubtype(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF)),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                "{1}{G}, {T}: You may put a creature card from your hand onto the battlefield. If it's a Wolf or Werewolf, untap this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "WildsongHowler";
    }
}
