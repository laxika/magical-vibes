package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "164")
public class SokenzanSmelter extends Card {

    public SokenzanSmelter() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay(AllOfCost.of(new PayManaCost("{1}"),
                                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "artifact", false)),
                        new CreateTokenEffect(
                                1,
                                "Construct",
                                3,
                                1,
                                CardColor.RED,
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(Keyword.HASTE),
                                Set.of(CardType.ARTIFACT))));
    }
}
