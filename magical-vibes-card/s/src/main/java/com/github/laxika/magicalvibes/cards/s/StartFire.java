package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

import java.util.List;
import java.util.Set;

/** Start // Fire, a split spell with one mode for each half. */
@CardRegistration(set = "MB1", collectorNumber = "101")
public class StartFire extends Card {

    public StartFire() {
        CreateTokenEffect warriors = new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.WARRIOR), Set.of(Keyword.VIGILANCE), Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Start - Create two 1/1 white Warrior creature tokens with vigilance",
                        warriors
                ).withManaCost("{2}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fire - Fire deals 2 damage divided as you choose among one or two targets",
                        List.<CardEffect>of(DealDividedDamageEffect.chosenAmongAnyTargets(2)),
                        null, null, 1, 2, false, null
                ).withManaCost("{1}{R}")
        )));
    }
}
