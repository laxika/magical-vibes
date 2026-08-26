package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.b.BahamutWardenOfLight;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "16")
public class DionBahamut extends Card {

    public DionBahamut() {
        setBackFaceCard(new BahamutWardenOfLight());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.KNIGHT))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}{W}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{4}{W}{W}, {T}: Exile Dion, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "BahamutWardenOfLight";
    }
}
