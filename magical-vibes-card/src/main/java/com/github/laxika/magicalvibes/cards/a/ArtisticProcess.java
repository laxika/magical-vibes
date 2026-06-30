package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "108")
public class ArtisticProcess extends Card {

    public ArtisticProcess() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Artistic Process deals 6 damage to target creature",
                        new DealDamageToTargetCreatureEffect(6)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Artistic Process deals 2 damage to each creature you don't control",
                        new MassDamageEffect(2, false, false, false,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 3/3 blue and red Elemental creature token with flying. It gains haste until end of turn",
                        new CreateTokenEffect(1, "Elemental", 3, 3,
                                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of(Keyword.HASTE))
                )
        )));
    }
}
