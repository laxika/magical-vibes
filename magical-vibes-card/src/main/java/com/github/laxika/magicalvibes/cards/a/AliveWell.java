package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "121")
public class AliveWell extends Card {

    public AliveWell() {
        CardEffect alive = new CreateTokenEffect(
                "Centaur", 3, 3, CardColor.GREEN, List.of(CardSubtype.CENTAUR), Set.of(), Set.of());
        CardEffect well = new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), 2));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Alive — Create a 3/3 green Centaur creature token.", alive
                ).withManaCost("{3}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Well — You gain 2 life for each creature you control.", well
                ).withManaCost("{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Create a 3/3 green Centaur creature token and gain 2 life for each creature you control.",
                        List.of(alive, well)
                ).withManaCost("{3}{G}{W}")
        )));
    }
}
