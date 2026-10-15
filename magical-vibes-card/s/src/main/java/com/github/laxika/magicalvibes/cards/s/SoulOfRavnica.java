package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "78")
public class SoulOfRavnica extends Card {

    public SoulOfRavnica() {
        List<CardEffect> drawForEachColor = List.of(
                drawIfColor(CardColor.WHITE),
                drawIfColor(CardColor.BLUE),
                drawIfColor(CardColor.BLACK),
                drawIfColor(CardColor.RED),
                drawIfColor(CardColor.GREEN)
        );

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                drawForEachColor,
                "{5}{U}{U}: Draw a card for each color among permanents you control."
        ));

        List<CardEffect> graveyardAbilityEffects = new ArrayList<>();
        graveyardAbilityEffects.add(new ExileSelfFromGraveyardCost());
        graveyardAbilityEffects.addAll(drawForEachColor);

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                graveyardAbilityEffects,
                "{5}{U}{U}, Exile this card from your graveyard: Draw a card for each color among permanents you control."
        ));
    }

    private static CardEffect drawIfColor(CardColor color) {
        return new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentColorInPredicate(Set.of(color))),
                new DrawCardEffect()
        );
    }
}
