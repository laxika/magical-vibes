package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "142")
@CardRegistration(set = "TMT", collectorNumber = "220")
@CardRegistration(set = "TMT", collectorNumber = "289")
@CardRegistration(set = "TMT", collectorNumber = "299")
public class DarkLeoShredder extends Card {

    public DarkLeoShredder() {
        addSneak("{W}{B}");

        var attackingNinjas = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.NINJA)));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES, attackingNinjas));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new CreateTokenEffect("Ninja", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.NINJA), Set.of(), Set.of()),
                ConditionalEffect.unless(
                        new ControlsPermanentCount(5, new PermanentHasSubtypePredicate(CardSubtype.NINJA)),
                        new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                                LoseLifeRecipient.TARGET_PLAYER))));
    }
}
