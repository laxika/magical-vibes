package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "353")
public class GhituEncampment extends Card {

    public GhituEncampment() {
        setEntersTapped(true);
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new AnimateLandEffect(2, 1, List.of(CardSubtype.WARRIOR), Set.of(Keyword.FIRST_STRIKE), CardColor.RED)),
                "{1}{R}: Ghitu Encampment becomes a 2/1 red Warrior creature with first strike until end of turn. It's still a land."
        ));
    }
}
