package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "345")
@CardRegistration(set = "THB", collectorNumber = "236")
@CardRegistration(set = "SLD", collectorNumber = "1505")
@CardRegistration(set = "PZA", collectorNumber = "17")
@CardRegistration(set = "LTC", collectorNumber = "353")
@CardRegistration(set = "LTC", collectorNumber = "383")
public class Shadowspear extends Card {

    public Shadowspear() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()));

        PermanentNotPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveKeywordEffect(Keyword.HEXPROOF, Subjects.allPermanents(), opponentPermanent),
                        new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.allPermanents(), opponentPermanent)
                ),
                "Permanents your opponents control lose hexproof and indestructible until end of turn."));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
