package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C20", collectorNumber = "6")
public class AkimTheSoaringWind extends Card {

    public AkimTheSoaringWind() {
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new OncePerTurnTriggerEffect(new CreateTokenEffect(
                        1, "Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{R}{W}",
                List.of(new GrantKeywordEffect(
                        Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES,
                        new PermanentIsTokenPredicate())),
                "{3}{U}{R}{W}: Creature tokens you control gain double strike until end of turn."
        ));
    }
}
