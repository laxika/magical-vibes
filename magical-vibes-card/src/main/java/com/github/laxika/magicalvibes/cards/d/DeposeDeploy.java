package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "225")
public class DeposeDeploy extends Card {

    public DeposeDeploy() {
        TargetFilter creature = TargetFilters.creature();
        CardEffect depose = new TapPermanentsEffect(TapUntapScope.TARGET);
        CardEffect createThopters = new CreateTokenEffect(
                2, "Thopter", 1, 1, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT));
        CardEffect gainLife = new GainLifeEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Depose — Tap target creature and draw a card",
                        List.of(depose, new DrawCardEffect()),
                        creature
                ).withManaCost("{1}{W/U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Deploy — Create two 1/1 colorless Thopter artifact creature tokens with flying, then you gain 1 life for each creature you control",
                        List.of(createThopters, gainLife)
                ).withManaCost("{2}{W}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Depose and then Deploy",
                        List.of(depose, new DrawCardEffect(), createThopters, gainLife),
                        List.of(creature)
                ).withManaCost("{3}{W}{U}{W/U}")
        )));
    }
}
