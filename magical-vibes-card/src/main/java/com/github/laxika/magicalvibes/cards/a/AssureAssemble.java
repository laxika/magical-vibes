package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "221")
public class AssureAssemble extends Card {

    public AssureAssemble() {
        TargetFilter creature = TargetFilters.creature();
        CardEffect assure = SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        CardEffect assemble = new CreateTokenEffect(
                3,
                "Elf Knight",
                2,
                2,
                CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.ELF, CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE),
                Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Assure — Put a +1/+1 counter on target creature. That creature gains indestructible until end of turn",
                        assure,
                        creature
                ).withManaCost("{G/W}{G/W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Assemble — Create three 2/2 green and white Elf Knight creature tokens with vigilance",
                        assemble
                ).withManaCost("{4}{G}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Assure and then Assemble",
                        List.of(assure, assemble),
                        List.of(creature)
                ).withManaCost("{4}{G}{W}{G/W}{G/W}")
        )));
    }
}
