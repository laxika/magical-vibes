package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "70")
@CardRegistration(set = "MSC", collectorNumber = "387")
public class WKabiShieldOfTheNation extends Card {

    public WKabiShieldOfTheNation() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new AllConditions(List.of(
                        new HasAttacker(new PermanentIsCommanderPredicate()),
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentMinManaValuePredicate(4))))
                )),
                new CreateTokenEffect("Rhino", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of())));
    }
}
