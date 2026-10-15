package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "114")
public class KnotvineMystic extends Card {

    public KnotvineMystic() {
        // {1}, {T}: Add {R}{G}{W}.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new AwardManaEffect(ManaColor.RED), new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {R}{G}{W}."));
    }
}
