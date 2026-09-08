package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokenForOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "154")
public class PharikaGodOfAffliction extends Card {

    public PharikaGodOfAffliction() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(Set.of(ManaColor.BLACK, ManaColor.GREEN), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));

        CreateTokenEffect snake = new CreateTokenEffect(
                CardType.CREATURE, 1, "Snake", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.SNAKE), Set.of(Keyword.DEATHTOUCH), Set.of(CardType.ENCHANTMENT),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(new ExileTargetCreatureCardCreateTokenForOwnerEffect(snake)),
                "{B}{G}: Exile target creature card from a graveyard. Its owner creates a 1/1 black and green Snake enchantment creature token with deathtouch."));
    }
}
