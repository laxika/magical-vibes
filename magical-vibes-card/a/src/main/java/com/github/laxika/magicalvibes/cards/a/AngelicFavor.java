package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "1")
public class AngelicFavor extends Card {

    public AngelicFavor() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT);
        addCastingOption(new AlternateHandCast(
                List.of(new TapUntappedPermanentsCost(1, new PermanentIsCreaturePredicate())),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                false));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Angel",
                4,
                4,
                CardColor.WHITE,
                null,
                List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                true,
                false,
                0,
                Set.of()));
    }
}
