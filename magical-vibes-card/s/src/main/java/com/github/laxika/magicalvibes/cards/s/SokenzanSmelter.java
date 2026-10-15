package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "164")
public class SokenzanSmelter extends Card {

    public SokenzanSmelter() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayPayManaAndSacrificePermanentEffect(
                        "{1}",
                        new PermanentIsArtifactPredicate(),
                        new CreateTokenEffect(
                                1,
                                "Construct",
                                3,
                                1,
                                CardColor.RED,
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(Keyword.HASTE),
                                Set.of(CardType.ARTIFACT)),
                        "an artifact"));
    }
}
