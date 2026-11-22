package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttachedToControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WOE", collectorNumber = "42")
@CardRegistration(set = "WOE", collectorNumber = "330")
public class AsinineAntics extends Card {

    public AsinineAntics() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}")), null, true));
        addSpellEffect(new CreateTokensAttachedToControlledCreaturesEffect(
                cursedRoleToken(), new PermanentIsCreaturePredicate(), Subjects.eachOpponent()));
    }

    private static CreateTokenEffect cursedRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Cursed",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(StaticAbility.of(new SetBasePowerToughnessEffect(1, 1, Subjects.attachedCreature()))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
