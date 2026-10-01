package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "130")
@CardRegistration(set = "WHO", collectorNumber = "417")
@CardRegistration(set = "WHO", collectorNumber = "541")
@CardRegistration(set = "WHO", collectorNumber = "735")
@CardRegistration(set = "WHO", collectorNumber = "1008")
@CardRegistration(set = "WHO", collectorNumber = "1132")
public class TheFugitiveDoctor extends Card {

    public TheFugitiveDoctor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new GrantFlashbackToTargetGraveyardCardEffect(
                                Set.of(CardType.INSTANT, CardType.SORCERY), "{2}{R}{G}"),
                        "a Clue"),
                "Sacrifice a Clue?"));
    }
}
