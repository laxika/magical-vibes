package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "212")
public class TitansNest extends Card {

    public TitansNest() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileCardFromGraveyardCost((CardType) null),
                        new AwardRestrictedManaEffect(
                                ManaColor.COLORLESS, 1, new ManaRestriction.ColoredSpellsWithoutX())),
                "Exile a card from your graveyard: Add {C}. Spend this mana only to cast a spell that's one or more colors without {X} in its mana cost."
        ));
    }
}
