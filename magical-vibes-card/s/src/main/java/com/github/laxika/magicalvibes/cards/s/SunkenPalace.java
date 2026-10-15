package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegisterNextSpellOrAbilityCopyEffect;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "81")
@CardRegistration(set = "M3C", collectorNumber = "133")
public class SunkenPalace extends Card {

    public SunkenPalace() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {1}{U}, {T}, Exile seven cards from your graveyard: Add {U}. When you spend this mana
        // to cast a spell or activate an ability, copy that spell or ability.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(
                        new ExileNCardsFromGraveyardCost(7, null),
                        new AwardManaEffect(ManaColor.BLUE),
                        new RegisterNextSpellOrAbilityCopyEffect()
                ),
                "{1}{U}, {T}, Exile seven cards from your graveyard: Add {U}. When you spend this mana to cast a spell or activate an ability, copy that spell or ability. You may choose new targets for the copy. (Mana abilities can't be copied.)"
        ));
    }
}
