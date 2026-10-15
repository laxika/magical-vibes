package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "34")
public class SkrelvsHive extends Card {

    public SkrelvsHive() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(1),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Mite",
                        1,
                        1,
                        null,
                        null,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                        Set.of(Keyword.TOXIC),
                        Set.of(CardType.ARTIFACT),
                        false,
                        false,
                        Map.of(
                                EffectSlot.STATIC, new CantBlockEffect(),
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)
                        ),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentPoisoned(3),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES,
                        new PermanentHasKeywordPredicate(Keyword.TOXIC))));
    }
}
