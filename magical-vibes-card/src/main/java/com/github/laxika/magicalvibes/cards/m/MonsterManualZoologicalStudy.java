package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.z.ZoologicalStudy;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1782")
public class MonsterManualZoologicalStudy extends Card {

    public MonsterManualZoologicalStudy() {
        setBackFaceCard(new ZoologicalStudy());
        addCastingOption(new AdventureCast("{2}{G}"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                "{1}{G}, {T}: You may put a creature card from your hand onto the battlefield."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ZoologicalStudy";
    }
}
