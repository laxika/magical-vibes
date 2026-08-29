package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "62")
public class SkeletalVampire extends Card {

    private static final CreateTokenEffect BAT_TOKENS = new CreateTokenEffect(
            2, "Bat", 1, 1, CardColor.BLACK, List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of());

    public SkeletalVampire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, BAT_TOKENS);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.BAT), "Sacrifice a Bat"),
                        BAT_TOKENS),
                "{3}{B}{B}, Sacrifice a Bat: Create two 1/1 black Bat creature tokens with flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.BAT), "Sacrifice a Bat"),
                        new RegenerateEffect()),
                "Sacrifice a Bat: Regenerate this creature."
        ));
    }
}
