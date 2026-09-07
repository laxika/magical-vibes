package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "37")
@CardRegistration(set = "SPM", collectorNumber = "253")
public class MysterioMasterOfIllusion extends Card {

    public MysterioMasterOfIllusion() {
        CreateTokenEffect illusionVillain = new CreateTokenEffect(
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        CountScope.CONTROLLER),
                "Illusion Villain", 3, 3, CardColor.BLUE,
                List.of(CardSubtype.ILLUSION, CardSubtype.VILLAIN), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenAndLinkToSourceEffect(illusionVillain, false));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ExileTokensCreatedWithSourceEffect());
    }
}
