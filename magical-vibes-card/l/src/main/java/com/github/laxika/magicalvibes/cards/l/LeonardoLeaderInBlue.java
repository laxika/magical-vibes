package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "16")
@CardRegistration(set = "TMT", collectorNumber = "196")
public class LeonardoLeaderInBlue extends Card {

    public LeonardoLeaderInBlue() {
        addSneak("{3}{W}{W}");
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new CastForAlternateCost(), new BoostAllOwnCreaturesEffect(2, 0)));
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{1}{W}: Leonardo gains first strike until end of turn."));
    }
}
