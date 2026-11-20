package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "74")
public class KingMacarTheGoldCursed extends Card {

    public KingMacarTheGoldCursed() {
        target(TargetFilters.creature()).addTrigger(Triggers.becomesUntapped(),
                new MayEffect(SequenceEffect.of(
                        new ExileTargetPermanentEffect(),
                        new ConditionalEffect(HappenedThisWay.exiled(), CreateTokenEffect.ofArtifactToken(1, "Gold",
                                List.of(CardSubtype.GOLD), List.of(new ActivatedAbility(
                                        false, null,
                                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                        "Sacrifice this token: Add one mana of any color."))))),
                "Exile target creature?"));
    }
}
