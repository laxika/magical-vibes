package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "39")
@CardRegistration(set = "HOC", collectorNumber = "79")
public class AndRilFlameOfTheWest extends Card {

    public AndRilFlameOfTheWest() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));

        CreateTokenEffect spirits = new CreateTokenEffect(
                CardType.CREATURE, 2, "Spirit", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of(),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
        CreateTokenEffect attackingSpirits = new CreateTokenEffect(
                CardType.CREATURE, 2, "Spirit", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of(),
                true, false, Map.of(), List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ATTACK, new ConditionalReplacementEffect(
                new EnchantedPermanentMatches(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        "equipped creature is legendary"),
                spirits, attackingSpirits));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
