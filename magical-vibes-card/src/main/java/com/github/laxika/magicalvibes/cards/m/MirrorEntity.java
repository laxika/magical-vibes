package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "31")
public class MirrorEntity extends Card {

    public MirrorEntity() {
        // {X}: Until end of turn, creatures you control have base power and toughness X/X
        // and gain all creature types. "Gain all creature types" is modeled by granting
        // Changeling, which the engine treats as having every creature type.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(
                        new SetAllOwnCreaturesBasePowerToughnessEffect(new XValue(), new XValue()),
                        new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.OWN_CREATURES)),
                "{X}: Until end of turn, creatures you control have base power and toughness X/X and gain all creature types."
        ));
    }
}
