package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "9")
@CardRegistration(set = "LCC", collectorNumber = "21")
public class IllustriousWanderglyph extends Card {

    public IllustriousWanderglyph() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCityBlessing(),
                new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate())))));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Gnome", 1, 1, null,
                List.of(CardSubtype.GNOME), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
